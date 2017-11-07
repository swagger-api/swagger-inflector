package io.swagger.oas.inflector.validators;


import io.swagger.v3.oas.models.parameters.Parameter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StringTypeValidator implements Validator {
    public void validate(Object o, Parameter parameter, Iterator<Validator> chain) throws ValidationException {
        if(o != null && parameter.getSchema() != null ) {
            if(parameter.getSchema().getEnum() != null && parameter.getSchema().getEnum().size() > 0) {
                List<?> values = parameter.getSchema().getEnum();
                Set<String> allowable = new LinkedHashSet<String>();
                for(Object obj : values) {
                    allowable.add(obj.toString());
                }
                if(!allowable.contains(o.toString())) {
                    throw new ValidationException()
                        .message(new ValidationMessage()
                            .code(ValidationError.UNACCEPTABLE_VALUE)
                            .message(parameter.getIn() + " parameter `" + parameter.getName() + "` value `" + o + "` is not in the allowable values `" + allowable + "`"));
                }
            };

            if("string".equals(parameter.getSchema().getType()) && ("date".equals(parameter.getSchema().getFormat()) || "date-time".equals(parameter.getSchema().getFormat()))) {
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
                    .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is not a valid " + parameter.getSchema().getFormat()));
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