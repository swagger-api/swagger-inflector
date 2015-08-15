package io.swagger.inflector.validators;

import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Iterator;

public class DateTimeValidator implements Validator {
    public void validate(Object o, Parameter parameter, Iterator<Validator> chain) throws ValidationException {
        if(o != null && parameter instanceof AbstractSerializableParameter) {
            AbstractSerializableParameter ap = (AbstractSerializableParameter) parameter;
            if("string".equals(ap.getType()) && ("date".equals(ap.getFormat()) || "date-time".equals(ap.getFormat()))) {
              if(o instanceof DateTime) {
                // TODO
              }
              else if(o instanceof LocalDate) {
                // TODO
              }
              else {
                throw new ValidationException()
                  .message(new ValidationMessage()
                    .code(ValidationError.INVALID_FORMAT)
                    .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is not a valid " + ap.getFormat()));
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