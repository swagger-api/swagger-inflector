package io.swagger.inflector.converters;

import io.swagger.inflector.validators.DefaultValidator;
import io.swagger.inflector.validators.NumericValidator;
import io.swagger.inflector.validators.ValidationException;
import io.swagger.inflector.validators.Validator;
import io.swagger.models.Model;
import io.swagger.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InputConverter {
    private static InputConverter INSTANCE = new InputConverter();
    private List<Validator> validationChain = new ArrayList<Validator>();
    private List<Converter> converterChain = new ArrayList<Converter>();

    static {
        INSTANCE.addValidator(new DefaultValidator());
        INSTANCE.addValidator(new NumericValidator());
        INSTANCE.addConverter(new DefaultConverter());
    }

    public static InputConverter getInstance() {
        return INSTANCE;
    }

    public void addConverter(Converter converter) {
        converterChain.add(converter);
    }

    public void addConverter(Converter converter, boolean first) {
        converterChain.add(0, converter);
    }

    public void addValidator(Validator validator) {
        validationChain.add(validator);
    }

    public void addValidator(Validator validator, boolean first) {
        validationChain.add(0, validator);
    }

    public Object convertAndValidate(List<String> value, Parameter parameter, Class<?> cls, Map<String, Model> definitions) throws ConversionException, ValidationException {
        Iterator<Converter> itr = converterChain.iterator();
        Object o = null;
        if(itr.hasNext()) {
            Converter converter = itr.next();
            o = converter.convert(value, parameter, cls, definitions, itr);
        }

        validate(o, parameter);
        return o;
    }

    public void validate(Object value, Parameter parameter) throws ValidationException {
        Iterator<Validator> itr = validationChain.iterator();

        if(itr.hasNext()) {
            Validator validator = itr.next();
            validator.validate(value, parameter, itr);
        }
    }
}