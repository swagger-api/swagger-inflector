package io.swagger.oas.inflector.validators;



import io.swagger.oas.models.parameters.Parameter;

import java.util.Iterator;

public class DefaultValidator implements Validator {
    public void validate(Object o, Parameter parameter, Iterator<Validator> chain) throws ValidationException {
        if (Boolean.TRUE.equals(parameter.getRequired())) {
            if(o == null) {
                throw new ValidationException()
                  .message(new ValidationMessage()
                    .code(ValidationError.MISSING_REQUIRED)
                    .message("missing required " + parameter.getIn() + " parameter `" + parameter.getName() + "`"));
            }
        }
        if(chain.hasNext()) {
            chain.next().validate(o, parameter, chain);
            return;
        }

        return;
    }
}