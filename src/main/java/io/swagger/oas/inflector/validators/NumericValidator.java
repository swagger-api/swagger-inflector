package io.swagger.oas.inflector.validators;


import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NumericValidator implements Validator {
    public void validate(Object o, Parameter parameter, Iterator<Validator> chain) throws ValidationException {
        if(o != null && parameter.getSchema() != null) {
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
                            .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is not in the allowable values `" + allowable + "`"));
                }
            };
            if(parameter.getSchema().getMaximum() != null) {
                double max = parameter.getSchema().getMaximum().doubleValue();
                Double value;
                try {
                    value = Double.parseDouble(o.toString());
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
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is greater than maximum allowed value `" + max + "`"));
                    }
                }
                else {
                    if(value > max) {
                        throw new ValidationException()
                          .message(new ValidationMessage()
                              .code(ValidationError.VALUE_OVER_MAXIMUM)
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is greater or equal to maximum allowed value `" + max + "`"));
                    }
                }
            }
            if(parameter.getSchema().getMinimum() != null) {
                double min = parameter.getSchema().getMinimum().doubleValue();
                Double value;
                try {
                    value = Double.parseDouble(o.toString());
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
                              .message(parameter.getIn() + " parameter `" + parameter.getName() + " value `" + o + "` is less than minimum allowed value `" + min + "`"));
                    }
                }
                else {
                    if(value < min) {
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

    public void validate(Object o, RequestBody body, Iterator<Validator> chain) throws ValidationException {
        if (body.getContent() != null) {
            for(String media: body.getContent().keySet()) {
                if (body.getContent().get(media) != null) {
                    MediaType mediaType = body.getContent().get(media);
                    if (o != null && mediaType.getSchema() != null) {
                        if (mediaType.getSchema().getProperties() != null) {
                            Map<String,Schema> values = mediaType.getSchema().getProperties();
                            Set<String> allowable = new LinkedHashSet<String>();
                            for (String name : values.keySet()) {
                                allowable.add(name);
                            }
                            if (!allowable.contains(o.toString())) {
                                throw new ValidationException()
                                        .message(new ValidationMessage()
                                                .code(ValidationError.UNACCEPTABLE_VALUE)
                                                .message( " request body `"  + " value `" + o + "` is not in the allowable values `" + allowable + "`"));
                            }
                        }

                        if (mediaType.getSchema().getMaximum() != null) {
                            double max = mediaType.getSchema().getMaximum().doubleValue();
                            Double value;
                            try {
                                value = Double.parseDouble(o.toString());
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
                                                    .message(" parameter `"  + " value `" + o + "` is greater than maximum allowed value `" + max + "`"));
                                }
                            } else {
                                if (value > max) {
                                    throw new ValidationException()
                                            .message(new ValidationMessage()
                                                    .code(ValidationError.VALUE_OVER_MAXIMUM)
                                                    .message(" parameter `" + " value `" + o + "` is greater or equal to maximum allowed value `" + max + "`"));
                                }
                            }
                        }
                        if (mediaType.getSchema().getMinimum() != null) {
                            double min = mediaType.getSchema().getMinimum().doubleValue();
                            Double value;
                            try {
                                value = Double.parseDouble(o.toString());
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
                                                    .message(" parameter `" + " value `" + o + "` is less than minimum allowed value `" + min + "`"));
                                }
                            } else {
                                if (value < min) {
                                    throw new ValidationException()
                                            .message(new ValidationMessage()
                                                    .code(ValidationError.VALUE_UNDER_MINIMUM)
                                                    .message(" parameter `" + " value `" + o + "` is less or equal to the minimum allowed value `" + min + "`"));
                                }
                            }
                        }

                    }
                }
            }
        }
        if(chain.hasNext()) {
            chain.next().validate(o, body, chain);
            return;
        }

        return;
    }
}