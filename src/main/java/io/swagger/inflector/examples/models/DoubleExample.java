package io.swagger.inflector.examples.models;

public class DoubleExample extends AbstractExample {
  private Double value;

  public DoubleExample(double value) {
    this.value = value;
  }
  
  public String asString() {
    if(value == null) {
      return null;
    }
    return String.valueOf(value);
  }

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }
}
