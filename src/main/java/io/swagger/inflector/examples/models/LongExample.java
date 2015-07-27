package io.swagger.inflector.examples.models;

public class LongExample extends AbstractExample {
  private Long value;

  public LongExample(long value) {
    this.value = value;
  }
  
  public String asString() {
    if(value == null) {
      return null;
    }
    return String.valueOf(value);
  }

  public Long getValue() {
    return value;
  }

  public void setValue(Long value) {
    this.value = value;
  }
}
