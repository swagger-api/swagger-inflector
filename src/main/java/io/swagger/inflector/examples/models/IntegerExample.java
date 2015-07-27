package io.swagger.inflector.examples.models;

public class IntegerExample extends AbstractExample {
  private Integer value;

  public IntegerExample(int value) {
    this.value = value;
  }

  public Integer asInt() {
    return value;
  }
  
  public String asString() {
    if(value == null) {
      return null;
    }
    return String.valueOf(value);
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }
}
