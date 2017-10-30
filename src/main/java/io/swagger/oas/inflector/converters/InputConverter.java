package io.swagger.oas.inflector.converters;



import io.swagger.oas.inflector.validators.DefaultValidator;
import io.swagger.oas.inflector.validators.NumericValidator;
import io.swagger.oas.inflector.validators.StringTypeValidator;
import io.swagger.oas.inflector.validators.ValidationException;
import io.swagger.oas.inflector.validators.Validator;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InputConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(InputConverter.class);
    private static InputConverter INSTANCE = new InputConverter();
    private List<Validator> validationChain = new ArrayList<Validator>();
    private List<Converter> converterChain = new ArrayList<Converter>();

    public static InputConverter getInstance() {
        return INSTANCE;
    }
    
    public List<Validator> getValidators() {
      return validationChain;
    }

    public List<Converter> getConverters() {
      return converterChain;
    }

    public void addConverter(Converter converter) {
      boolean matched = false;
      for(Converter c : converterChain) {
        if(c.getClass().getName().equals(converter.getClass().getName())) {
          matched = true;
        }
      }
      if(!matched) {
        LOGGER.debug("adding " + converter.getClass().getName());
        converterChain.add(converter);
      }
      else {
        LOGGER.debug("skipping " + converter.getClass().getName());
      }
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

    public Object convertAndValidate(List<String> value, Parameter parameter, Class<?> cls, Map<String, Schema> definitions) throws ConversionException, ValidationException {
        Iterator<Converter> itr = converterChain.iterator();
        Object o = null;
        if(itr.hasNext()) {
            Converter converter = itr.next();
            LOGGER.debug("using converter `" + converter.getClass().getName() + "`");
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

    public Object convertAndValidate(List<String> value, RequestBody body, Class<?> cls, Map<String, Schema> definitions) throws ConversionException, ValidationException {
        Iterator<Converter> itr = converterChain.iterator();
        Object o = null;
        if(itr.hasNext()) {
            Converter converter = itr.next();
            LOGGER.debug("using converter `" + converter.getClass().getName() + "`");
            o = converter.convert(value, body, cls, definitions, itr);
        }

        validate(o, body);
        return o;
    }

    public void validate(Object value, RequestBody body) throws ValidationException {
        Iterator<Validator> itr = validationChain.iterator();

        if(itr.hasNext()) {
            Validator validator = itr.next();
            validator.validate(value, body, itr);
        }
    }
}