package io.swagger.oas.inflector.validators;


import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StringTypeValidator implements Validator {
    public void validate(Object argument, Parameter parameter, Iterator<Validator> chain) throws ValidationException {
        if(argument != null && parameter.getSchema() != null ) {
            if(parameter.getSchema().getEnum() != null && parameter.getSchema().getEnum().size() > 0) {
                List<?> values = parameter.getSchema().getEnum();
                Set<String> allowable = new LinkedHashSet<String>();
                for(Object obj : values) {
                    allowable.add(obj.toString());
                }
                if(!allowable.contains(argument.toString())) {
                    throw new ValidationException()
                        .message(new ValidationMessage()
                            .code(ValidationError.UNACCEPTABLE_VALUE)
                            .message(parameter.getIn() + " parameter `" + parameter.getName() + "` value `" + argument + "` is not in the allowable values `" + allowable + "`"));
                }
            }

            if("string".equals(parameter.getSchema().getType()) && ("date".equals(parameter.getSchema().getFormat()) || "date-time".equals(parameter.getSchema().getFormat()))) {
              if(argument instanceof DateTime) {
                // TODO
              }
              else if(argument instanceof LocalDate) {
                // TODO
              }
              else {
                throw new ValidationException()
                  .message(new ValidationMessage()
                    .code(ValidationError.INVALID_FORMAT)
                    .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + argument + "` is not a valid " + parameter.getSchema().getFormat()));
              }
            }
        }
        if(chain.hasNext()) {
            chain.next().validate(argument, parameter, chain);
            return;
        }

        return;
    }

    public void validate(Object argument, RequestBody body, Iterator<Validator> chain) throws ValidationException {
        if (body.getContent() != null) {
            for(String media: body.getContent().keySet()) {
                if (body.getContent().get(media) != null) {
                    MediaType mediaType = body.getContent().get(media);
                    if (argument != null && mediaType.getSchema() != null) {
                        if(mediaType.getSchema().getEnum() != null && mediaType.getSchema().getEnum().size() > 0) {
                            List<?> values = mediaType.getSchema().getEnum();
                            Set<String> allowable = new LinkedHashSet<String>();
                            for(Object obj : values) {
                                allowable.add(obj.toString());
                            }
                            if(!allowable.contains(argument.toString())) {
                                throw new ValidationException()
                                        .message(new ValidationMessage()
                                                .code(ValidationError.UNACCEPTABLE_VALUE)
                                                .message(" parameter  value `" + argument + "` is not in the allowable values `" + allowable + "`"));
                            }
                        };

                        if ("string".equals(mediaType.getSchema().getType()) && ("date".equals(mediaType.getSchema().getFormat()) || "date-time".equals(mediaType.getSchema().getFormat()))) {
                            if (argument instanceof DateTime) {
                                // TODO
                            } else if (argument instanceof LocalDate) {
                                // TODO
                            } else {
                                throw new ValidationException()
                                        .message(new ValidationMessage()
                                                .code(ValidationError.INVALID_FORMAT)
                                                .message( " parameter `" +  " value `" + argument + "` is not a valid " + mediaType.getSchema().getFormat()));
                            }
                        }
                    }
                }
            }
        }
        if(chain.hasNext()) {
            chain.next().validate(argument, body, chain);
            return;
        }

        return;
    }
}