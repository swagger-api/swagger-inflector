package io.swagger.inflector.examples.models;

import io.swagger.inflector.processors.JsonExampleDeserializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using=JsonExampleDeserializer.class)
public class StringExample extends AbstractExample {
  private String value;
  
  public StringExample(String value) {
    this.value = value;
  }

  public String textValue() {
    return value;
  }

  public String asString() {
    return value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
