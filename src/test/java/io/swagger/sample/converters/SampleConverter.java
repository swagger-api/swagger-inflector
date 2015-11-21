package io.swagger.sample.converters;

import io.swagger.inflector.converters.ConversionException;
import io.swagger.inflector.converters.Converter;
import io.swagger.models.Model;
import io.swagger.models.parameters.Parameter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SampleConverter implements Converter {
  public Object convert(List<String> value, Parameter parameter, Class<?> cls, Map<String, Model> definitions, Iterator<Converter> chain)
      throws ConversionException {
    if(chain.hasNext()) {
      return chain.next().convert(value, parameter, cls, definitions, chain);
    }
    return null;
  }
}