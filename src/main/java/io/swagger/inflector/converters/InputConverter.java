package io.swagger.inflector.converters;

import io.swagger.inflector.validators.*;
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

    public static InputConverter getInstance() {
        return INSTANCE;
    }

    public void addConverter(Converter converter) {
      boolean matched = false;
      for(Converter c : converterChain) {
        if(c.getClass().getName().equals(converter.getClass().getName())) {
          matched = true;
        }
      }
      if(!matched)
         converterChain.add(converter);
    }

    public void addConverter(Converter converter, boolean first) {
      boolean matched = false;
      for(Converter c : converterChain) {
        if(c.getClass().getName().equals(converter.getClass().getName())) {
          matched = true;
        }
      }
      if(!matched)
        converterChain.add(0, converter);
    }

    public InputConverter defaultConverters() {
        converterChain.clear();
        converterChain.add(new DefaultConverter());
        return this;
    }

    public void addValidator(Validator validator) {
        boolean matched = false;
        for(Validator v : validationChain) {
          if(v.getClass().getName().equals(validator.getClass().getName())) {
            matched = true;
          }
        }
        if(!matched)
            validationChain.add(validator);
    }

    public void addValidator(Validator validator, boolean first) {
      boolean matched = false;
      for(Validator v : validationChain) {
        if(v.getClass().getName().equals(validator.getClass().getName())) {
          matched = true;
        }
      }
      if(!matched)
          validationChain.add(0, validator);
    }

    public InputConverter defaultValidators() {
        validationChain.clear();
        validationChain.add(new DefaultValidator());
        validationChain.add(new NumericValidator());
        validationChain.add(new StringTypeValidator());
        return this;
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