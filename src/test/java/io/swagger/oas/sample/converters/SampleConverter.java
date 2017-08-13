package io.swagger.oas.sample.converters;

import io.swagger.oas.inflector.converters.ConversionException;
import io.swagger.oas.inflector.converters.Converter;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SampleConverter implements Converter {
  public Object convert(List<String> value, Parameter parameter, Class<?> cls, Map<String, Schema> definitions, Iterator<Converter> chain)
      throws ConversionException {
    if(chain.hasNext()) {
      return chain.next().convert(value, parameter, cls, definitions, chain);
    }
    return null;
  }
}