package io.swagger.inflector.validators;

import io.swagger.models.parameters.Parameter;

import java.util.Iterator;

public class DefaultValidator implements Validator {
    public void validate(Object o, Parameter parameter, Iterator<Validator> chain) throws ValidationException {
        if(parameter.getRequired()) {
            if(o == null) {
                throw new ValidationException()
                  .message(new ValidationMessage()
                    .code(ValidationError.MISSING_REQUIRED)
                    .message(parameter.getName() + ": " + parameter.getIn()));
            }
        }
        if(chain.hasNext()) {
            chain.next().validate(o, parameter, chain);
            return;
        }

        return;
    }
}