package io.swagger.oas.inflector.validators;



import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.Iterator;

public class DefaultValidator implements Validator {
    public void validate(Object argument, Parameter parameter, Iterator<Validator> chain) throws ValidationException {
        if (Boolean.TRUE.equals(parameter.getRequired())) {
            if(argument == null) {
                throw new ValidationException()
                  .message(new ValidationMessage()
                    .code(ValidationError.MISSING_REQUIRED)
                    .message("missing required " + parameter.getIn() + " parameter `" + parameter.getName() + "`"));
            }
        }
        if(chain.hasNext()) {
            chain.next().validate(argument, parameter, chain);
            return;
        }

        return;
    }

    public void validate(Object argument, RequestBody body, Iterator<Validator> chain) throws ValidationException {
        if (Boolean.TRUE.equals(body.getRequired())) {
            if(argument == null) {
                throw new ValidationException()
                        .message(new ValidationMessage()
                                .code(ValidationError.MISSING_REQUIRED)
                                .message("missing required  parameter"));
            }
        }
        if(chain.hasNext()) {
            chain.next().validate(argument, body, chain);
            return;
        }

        return;
    }
}