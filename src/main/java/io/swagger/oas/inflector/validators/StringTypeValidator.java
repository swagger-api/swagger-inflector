package io.swagger.oas.inflector.validators;


import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
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
            Set<String> allowable = validateAllowedValues(argument, parameter.getSchema());
            if(allowable!= null){
                throw new ValidationException()
                    .message(new ValidationMessage()
                            .code(ValidationError.UNACCEPTABLE_VALUE)
                            .message(parameter.getIn() + " parameter `" + parameter.getName() + "` value `" + argument + "` is not in the allowable values `" + allowable + "`"));
            }

            if(validateFormat(argument,parameter.getSchema())){
                throw new ValidationException()
                    .message(new ValidationMessage()
                            .code(ValidationError.INVALID_FORMAT)
                            .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + argument + "` is not a valid " + parameter.getSchema().getFormat()));
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
                    Set<String> allowable = validateAllowedValues(argument, mediaType.getSchema());
                    if(allowable != null){
                        throw new ValidationException()
                            .message(new ValidationMessage()
                                    .code(ValidationError.UNACCEPTABLE_VALUE)
                                    .message(" parameter  value `" + argument + "` is not in the allowable values `" + allowable + "`"));
                    }
                    if (validateFormat(argument,mediaType.getSchema())){
                        throw new ValidationException()
                            .message(new ValidationMessage()
                                    .code(ValidationError.INVALID_FORMAT)
                                    .message( " parameter value `" + argument + "` is not a valid " + mediaType.getSchema().getFormat()));
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

    public Set<String> validateAllowedValues(Object argument, Schema schema){
        if (argument != null && schema != null) {
            if (schema.getEnum() != null && schema.getEnum().size() > 0) {
                List<?> values = schema.getEnum();
                Set<String> allowable = new LinkedHashSet<>();
                for (Object obj : values) {
                    allowable.add(obj.toString());
                }
                if (!allowable.contains(argument.toString())) {
                    return allowable;
                }
            }
        }
        return  null;
    }

    public boolean validateFormat(Object argument, Schema schema) throws ValidationException{
        if (argument != null && schema != null) {
            if ("string".equals(schema.getType()) && ("date".equals(schema.getFormat()) || "date-time".equals(schema.getFormat()))) {
                if (argument instanceof DateTime) {

                } else if (argument instanceof LocalDate) {
                    
                } else {
                    return true;
                }
            }

        }
        return false;
    }
}