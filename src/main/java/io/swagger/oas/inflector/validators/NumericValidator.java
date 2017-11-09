package io.swagger.oas.inflector.validators;


import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;


import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NumericValidator implements Validator {
    public void validate(Object argument, Parameter parameter, Iterator<Validator> chain) throws ValidationException {
        if(argument != null && parameter.getSchema() != null) {
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
                            .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + argument + "` is not in the allowable values `" + allowable + "`"));
                }
            };
            if(parameter.getSchema().getMaximum() != null) {
                double max = parameter.getSchema().getMaximum().doubleValue();
                Double value;
                try {
                    value = Double.parseDouble(argument.toString());
                }
                catch (NumberFormatException e) {
                    throw new ValidationException()
                            .message(new ValidationMessage()
                                    .code(ValidationError.INVALID_FORMAT)
                                    .message(parameter.getIn() + " parameter `" + parameter.getName() + " is not a compatible number"));
                }
                if(parameter.getSchema().getExclusiveMaximum() != null && parameter.getSchema().getExclusiveMaximum()) {
                    if(value >= max) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_OVER_MAXIMUM)
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + argument + "` is greater than maximum allowed value `" + max + "`"));
                    }
                }
                else {
                    if(value > max) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_OVER_MAXIMUM)
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + argument + "` is greater or equal to maximum allowed value `" + max + "`"));
                    }
                }
            }
            if(parameter.getSchema().getMinimum() != null) {
                double min = parameter.getSchema().getMinimum().doubleValue();
                Double value;
                try {
                    value = Double.parseDouble(argument.toString());
                }
                catch (NumberFormatException e) {
                    throw new ValidationException()
                            .message(new ValidationMessage()
                                    .code(ValidationError.INVALID_FORMAT)
                                    .message(parameter.getIn() + " parameter `" + parameter.getName() + " is not a compatible number"));
                }
                if(parameter.getSchema().getExclusiveMinimum() != null && parameter.getSchema().getExclusiveMinimum()) {
                    if(value <= min) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_UNDER_MINIMUM)
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + argument + "` is less than minimum allowed value `" + min + "`"));
                    }
                }
                else {
                    if(value < min) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_UNDER_MINIMUM)
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + argument + "` is less or equal to the minimum allowed value `" + min + "`"));
                    }
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
                                                .message( " parameter  value `" + argument + "` is not in the allowable values `" + allowable + "`"));
                            }
                        };

                        if (mediaType.getSchema().getMaximum() != null) {
                            double max = mediaType.getSchema().getMaximum().doubleValue();
                            Double value;
                            try {
                                value = Double.parseDouble(argument.toString());
                            } catch (NumberFormatException e) {
                                throw new ValidationException()
                                        .message(new ValidationMessage()
                                                .code(ValidationError.INVALID_FORMAT)
                                                .message(" parameter `" +  " is not a compatible number"));
                            }
                            if (mediaType.getSchema().getExclusiveMaximum() != null && mediaType.getSchema().getExclusiveMaximum()) {
                                if (value >= max) {
                                    throw new ValidationException()
                                            .message(new ValidationMessage()
                                                    .code(ValidationError.VALUE_OVER_MAXIMUM)
                                                    .message(" parameter `"  + " value `" + argument + "` is greater than maximum allowed value `" + max + "`"));
                                }
                            } else {
                                if (value > max) {
                                    throw new ValidationException()
                                            .message(new ValidationMessage()
                                                    .code(ValidationError.VALUE_OVER_MAXIMUM)
                                                    .message(" parameter `" + " value `" + argument + "` is greater or equal to maximum allowed value `" + max + "`"));
                                }
                            }
                        }
                        if (mediaType.getSchema().getMinimum() != null) {
                            double min = mediaType.getSchema().getMinimum().doubleValue();
                            Double value;
                            try {
                                value = Double.parseDouble(argument.toString());
                            } catch (NumberFormatException e) {
                                throw new ValidationException()
                                        .message(new ValidationMessage()
                                                .code(ValidationError.INVALID_FORMAT)
                                                .message( " parameter `" + " is not a compatible number"));
                            }
                            if (mediaType.getSchema().getExclusiveMinimum() != null && mediaType.getSchema().getExclusiveMinimum()) {
                                if (value <= min) {
                                    throw new ValidationException()
                                            .message(new ValidationMessage()
                                                    .code(ValidationError.VALUE_UNDER_MINIMUM)
                                                    .message(" parameter `" + " value `" + argument + "` is less than minimum allowed value `" + min + "`"));
                                }
                            } else {
                                if (value < min) {
                                    throw new ValidationException()
                                            .message(new ValidationMessage()
                                                    .code(ValidationError.VALUE_UNDER_MINIMUM)
                                                    .message(" parameter `" + " value `" + argument + "` is less or equal to the minimum allowed value `" + min + "`"));
                                }
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