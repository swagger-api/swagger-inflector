package io.swagger.oas.inflector.converters;



import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Converter {
    Object convert(List<String> value, Parameter parameter, Class<?> cls, Map<String, Schema> definitions, Iterator<Converter> chain) throws ConversionException;
    Object convert(List<String> value, RequestBody body, Class<?> cls, Map<String, Schema> definitions, Iterator<Converter> chain) throws ConversionException;

}