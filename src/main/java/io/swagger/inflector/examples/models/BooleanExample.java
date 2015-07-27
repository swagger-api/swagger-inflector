package io.swagger.inflector.examples.models;

public class BooleanExample extends AbstractExample {
  private Boolean value;

  public BooleanExample(boolean value) {
    this.value = value;
  }

  public String asString() {
    if(value == null) {
      return null;
    }
    return value.toString();
  }

  public Boolean getValue() {
    return value;
  }

  public void setValue(Boolean value) {
    this.value = value;
  }
}
