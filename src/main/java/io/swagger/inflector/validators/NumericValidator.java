package io.swagger.inflector.validators;

import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.Parameter;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NumericValidator implements Validator {
    public void validate(Object o, Parameter parameter, Iterator<Validator> chain) throws ValidationException {
        if(o != null && parameter instanceof AbstractSerializableParameter) {
            AbstractSerializableParameter<?> ap = (AbstractSerializableParameter<?>) parameter;
            if(ap.getEnum() != null && ap.getEnum().size() > 0) {
                List<?> values = ap.getEnum();
                Set<String> allowable = new LinkedHashSet<String>();
                for(Object obj : values) {
                    allowable.add(obj.toString());
                }
                if(!allowable.contains(o.toString())) {
                    throw new ValidationException()
                        .message(new ValidationMessage()
                            .code(ValidationError.UNACCEPTABLE_VALUE)
                            .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is not in the allowable values `" + allowable + "`"));
                }
            };
            if(ap.getMaximum() != null) {
                double max = ap.getMaximum();
                if(ap.isExclusiveMaximum() != null && ap.isExclusiveMaximum()) {
                    if(Double.parseDouble(o.toString()) >= max) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_OVER_MAXIMUM)
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is greater than maximum allowed value `" + max + "`"));
                    }
                }
                else {
                    if(Double.parseDouble(o.toString()) > max) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_OVER_MAXIMUM)
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is greater or equal to maximum allowed value `" + max + "`"));
                    }
                }
            }
            if(ap.getMinimum() != null) {
                double min = ap.getMinimum();
                if(ap.isExclusiveMinimum() != null && ap.isExclusiveMinimum()) {
                    if(Double.parseDouble(o.toString()) <= min) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_UNDER_MINIMUM)
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is less than minimum allowed value `" + min + "`"));
                    }
                }
                else {
                    if(Double.parseDouble(o.toString()) < min) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_UNDER_MINIMUM)
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is less or equal to the minimum allowed value `" + min + "`"));
                    }
                }
            }
            
        }
        if(chain.hasNext()) {
            chain.next().validate(o, parameter, chain);
            return;
        }

        return;
    }
}