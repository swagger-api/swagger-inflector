package io.swagger.test.examples;

import io.swagger.inflector.examples.models.AbstractExample;
import io.swagger.inflector.examples.models.Example;

import java.util.ArrayList;
import java.util.List;

public class MapRepresentation extends AbstractExample {
  List<Example> values = null;

  public void add(Example value) {
    if(values == null) {
      values = new ArrayList<Example>();
    }
    values.add(value);
  }

  @Override
  public String asString() {
    return "NOT IMPLEMENTED";
  }
  
  public List<Example> getAdditionalProperties() {
    if(values == null) {
      return new ArrayList<Example>();
    }
    return values;
  }
}
