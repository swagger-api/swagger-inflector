package io.swagger.inflector.validators;

import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;

import java.util.Iterator;

public class NumericValidator implements Validator {
    public void validate(Object o, Parameter parameter, Iterator<Validator> chain) throws ValidationException {
        if(o != null && parameter instanceof AbstractSerializableParameter) {
            AbstractSerializableParameter ap = (AbstractSerializableParameter) parameter;
            if(ap.getMaximum() != null) {
                double max = ap.getMaximum();
                if(ap.isExclusiveMaximum() != null && ap.isExclusiveMaximum()) {
                    if(Double.parseDouble(o.toString()) > max) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_OVER_MAXIMUM)
                              .message("`" + o + "` is greater than maximum allowed value `" + max + "`"));
                    }
                }
                else {
                    if(Double.parseDouble(o.toString()) >= max) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_OVER_MAXIMUM)
                              .message("`" + o + "` is greater or equal to maximum allowed value `" + max + "`"));
                    }
                }
            }
            if(ap.getMinimum() != null) {
                double min = ap.getMinimum();
                if(ap.isExclusiveMinimum() != null && ap.isExclusiveMinimum()) {
                    if(Double.parseDouble(o.toString()) < min) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_UNDER_MINIMUM)
                              .message("`" + o + "` is less than minimum allowed value `" + min + "`"));
                    }
                }
                else {
                    if(Double.parseDouble(o.toString()) <= min) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_UNDER_MINIMUM)
                              .message("`" + o + "` is less or equal to the minimum allowed value `" + min + "`"));
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