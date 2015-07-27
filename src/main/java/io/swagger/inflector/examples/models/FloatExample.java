package io.swagger.inflector.examples.models;

public class FloatExample extends AbstractExample {
  private Float value;

  public FloatExample(float value) {
    this.value = value;
  }

  public String asString() {
    if(value == null) {
      return null;
    }
    return String.valueOf(value);
  }

  public Float getValue() {
    return value;
  }

  public void setValue(Float value) {
    this.value = value;
  }
}
