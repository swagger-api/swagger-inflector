package io.swagger.inflector.examples.models;

import io.swagger.inflector.processors.JsonExampleDeserializer;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using=JsonExampleDeserializer.class)
public class ObjectExample extends AbstractExample {
  private Map<String, Example> values;

  public void put(String key, Example value) {
    if(values == null) {
      values = new LinkedHashMap<String, Example>();
    }
    values.put(key,  value);
  }
  
  public Set<String> keySet() {
    if(values == null) {
      return new HashSet<String>();
    }
    return values.keySet();
  }
  
  public Object get(String key) {
    if(values != null) {
      return values.get(key);
    }
    return null;
  }
  
  public String asString() {
    if(values == null) {
      return null;
    }
    return "NOT IMPLEMENTED";
  }
  
  public Map<String, Example> getValues() {
    return values;
  }
  public void setValues(Map<String, Example> values) {
    this.values = values;
  }
}
