package io.swagger.oas.inflector.converters;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.swagger.oas.inflector.utils.ReflectionUtils;
import io.swagger.oas.inflector.validators.ValidationError;
import io.swagger.oas.inflector.validators.ValidationMessage;

import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.QueryParameter;
import io.swagger.util.Json;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class DefaultConverter extends ReflectionUtils implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConverter.class);

    private Map<String, Schema> definitions;

    public DefaultConverter(){}

    public Object convert(List<String> value, Parameter parameter, Class<?> cls, Map<String, Schema> definitions, Iterator<Converter> chain) throws ConversionException {

        return coerceValue(value, parameter, cls);

    }
    
    

    public Object coerceValue(List<String> o, Parameter parameter, Class<?> cls) throws ConversionException {
        if (o == null || o.size() == 0) {
            return null;
        }

        LOGGER.debug("casting `" + o + "` to " + cls);
        if (List.class.equals(cls)) {
            if (parameter.getSchema() != null) {
                List<Object> output = new ArrayList<>();
                if (parameter.getSchema() instanceof ArraySchema) {
                    ArraySchema arraySchema = ((ArraySchema) parameter.getSchema());
                    Schema inner = arraySchema.getItems();


                    // TODO: this does not need to be done this way, update the helper method
                    Parameter innerParam = new QueryParameter();
                    innerParam.setSchema(inner);
                    JavaType innerClass = getTypeFromParameter(innerParam, definitions);
                    for (String obj : o) {
                        String[] parts = new String[0];
                        if (Parameter.StyleEnum.FORM.equals(parameter.getStyle()) && !StringUtils.isEmpty(obj) && parameter.getExplode() == false ) {
                            parts = obj.split(",");
                        }
                        if (Parameter.StyleEnum.PIPEDELIMITED.equals(parameter.getStyle()) && !StringUtils.isEmpty(obj)) {
                            parts = obj.split("|");
                        }
                        if (Parameter.StyleEnum.SPACEDELIMITED.equals(parameter.getStyle()) && !StringUtils.isEmpty(obj)) {
                            parts = obj.split(" ");
                        }
                        if (Parameter.StyleEnum.FORM.equals(parameter.getStyle()) && !StringUtils.isEmpty(obj) && parameter.getExplode() == true) {
                            parts = new String[1];
                            parts[0]= obj;
                        }
                        for (String p : parts) {
                            Object ob = cast(p, inner, innerClass);
                            if (ob != null) {
                                output.add(ob);
                            }
                        }
                    }
                }
                return output;
            }
        } else if (parameter.getSchema() != null) {
            TypeFactory tf = Json.mapper().getTypeFactory();

            return cast(o.get(0), parameter.getSchema(), tf.constructType(cls));

        }
        return null;
    }

    public Object cast(List<String> o, Parameter parameter, JavaType javaType, Map<String, Schema> definitions) throws ConversionException {
        if (o == null || o.size() == 0) {
            return null;
        }
        Class<?> cls = javaType.getRawClass();

        LOGGER.debug("converting array `" + o + "` to `" + cls + "`");
        if (javaType.isArrayType()) {
            if (parameter.getSchema() != null) {
                List<Object> output = new ArrayList<>();
                if (parameter.getSchema() instanceof ArraySchema) {
                    ArraySchema arraySchema = (ArraySchema) parameter.getSchema();
                    if (arraySchema.getItems() != null) {
                        Schema inner = arraySchema.getItems();

                        // TODO: this does not need to be done this way, update the helper method
                        Parameter innerParam = new QueryParameter().schema(inner);
                        JavaType innerClass = getTypeFromParameter(innerParam, definitions);
                        for (String obj : o) {
                            String[] parts = new String[0];
                            CSVFormat format = null;
                            if (Parameter.StyleEnum.FORM.equals(parameter.getStyle()) && !StringUtils.isEmpty(obj) && parameter.getExplode() == false) {
                                format = CSVFormat.DEFAULT;
                            } else if (Parameter.StyleEnum.PIPEDELIMITED.equals(parameter.getStyle()) && !StringUtils.isEmpty(obj)) {
                                format = CSVFormat.newFormat('|').withQuote('"');
                            } else if (Parameter.StyleEnum.SPACEDELIMITED.equals(parameter.getStyle()) && !StringUtils.isEmpty(obj)) {
                                format = CSVFormat.newFormat(' ').withQuote('"');
                            }
                            if (format != null) {
                                try {
                                    for (CSVRecord record : CSVParser.parse(obj, format).getRecords()) {
                                        List<String> it = new ArrayList<String>();
                                        for (Iterator<String> x = record.iterator(); x.hasNext(); ) {
                                            it.add(x.next());
                                        }
                                        parts = it.toArray(new String[it.size()]);
                                    }
                                } catch (IOException e) {
                                }
                            } else {
                                parts = new String[1];
                                parts[0] = obj;
                            }
                            for (String p : parts) {
                                Object ob = cast(p, inner, innerClass);
                                if (ob != null) {
                                    output.add(ob);
                                }
                            }

                        }

                        return output;
                    }
                }
            }
        } else if (parameter != null) {
            return cast(o.get(0), parameter.getSchema(), javaType);
        }
        return null;
    }

    public Object cast(String o, Schema property, JavaType javaType) throws ConversionException {
        if (o == null || javaType == null) {
            return null;
        }
        Class<?> cls = javaType.getRawClass();
        LOGGER.debug("coercing `" + o + "` to `" + cls + "`");
        try {
            if (Integer.class.equals(cls)) {
                return Integer.parseInt(o);
            }
            if (Long.class.equals(cls)) {
                return Long.parseLong(o);
            }
            if (Float.class.equals(cls)) {
                return Float.parseFloat(o);
            }
            if (Double.class.equals(cls)) {
                return Double.parseDouble(o);
            }
            if (String.class.equals(cls)) {
                return o;
            }
            if (Boolean.class.equals(cls)) {
                if ("1".equals(o)) {
                    return Boolean.TRUE;
                }
                if ("0".equals(o)) {
                    return Boolean.FALSE;
                }
                return Boolean.parseBoolean(o);
            }
            if (UUID.class.equals(cls)) {
                return UUID.fromString(o);
            }
            if(LocalDate.class.equals(cls)) {
                return LocalDate.parse(o);
            }
            if(DateTime.class.equals(cls)) {
                return DateTime.parse(o);
            }
        } catch (NumberFormatException e) {
            LOGGER.debug("couldn't coerce `" + o + "` to type " + cls);
            throw new ConversionException()
              .message(new ValidationMessage()
                .code(ValidationError.INVALID_FORMAT)
                .message("couldn't convert `" + o + "` to type `" + cls + "`"));
        } catch (IllegalArgumentException e) {
            LOGGER.debug("couldn't coerce `" + o + "` to type " + cls);
            throw new ConversionException()
              .message(new ValidationMessage()
                .code(ValidationError.INVALID_FORMAT)
                .message("couldn't convert `" + o + "` to type `" + cls + "`"));
        }
        return null;
    }
}
