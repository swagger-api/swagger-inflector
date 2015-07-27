package io.swagger.inflector.examples.models;

import java.util.ArrayList;
import java.util.List;

public class ArrayExample extends AbstractExample {
  List<Example> values = null;

  public void add(Example value) {
    if(values == null) {
      values = new ArrayList<Example>();
    }
    values.add(value);
  }
  
  public String asString() {
    return "NOT IMPLEMENTED";
  }

  public List<Example> getItems() {
    if(values == null) {
      return new ArrayList<Example>();
    }
    return values;
  }
}
