package io.swagger.oas.inflector.converters;



import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Converter {
    Object convert(List<String> value, Parameter parameter, Class<?> cls, Map<String, Schema> definitions, Iterator<Converter> chain) throws ConversionException;
}