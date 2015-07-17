package io.swagger.inflector.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.swagger.models.Model;
import io.swagger.models.properties.*;

public class ExampleBuilder {
  public static Object fromProperty(Property property, Map<String, Model> definitions) {
    return fromProperty(property, definitions, new HashSet<String>());
  }
  public static Object fromProperty(Property property, Map<String, Model> definitions, Set<String> processedModels) {
    if(property instanceof RefProperty) {
      RefProperty ref = (RefProperty) property;
      Model model = definitions.get(ref.getSimpleRef());
      if(model != null) {
        if(model.getExample() != null) {
          return model.getExample();
        }
      }
    }
    if(property instanceof StringProperty) {
      if(property.getExample() != null) {
        return property.getExample();
      }
      return "string";
    }
    if(property instanceof IntegerProperty) {
      if(property.getExample() != null) {
        return Integer.parseInt(property.getExample());
      }
      return new Integer(0);
    }
    if(property instanceof LongProperty) {
      if(property.getExample() != null) {
        return Long.parseLong(property.getExample());
      }
      return new Long(0);
    }
    if(property instanceof FloatProperty) {
      if(property.getExample() != null) {
        return Float.parseFloat(property.getExample());
      }
      return new Float(1.1);
    }
    if(property instanceof DoubleProperty) {
      if(property.getExample() != null) {
        return Double.parseDouble(property.getExample());
      }
      return new Double(1.23);
    }
    if(property instanceof BooleanProperty) {
      if(property.getExample() != null) {
        return Boolean.parseBoolean(property.getExample());
      }
      return Boolean.TRUE;
    }

    // TODO: Array, Date, DateTime, Decimal, Email, File, Float, Map, Object, UUID
    
    if(property instanceof RefProperty) {
      RefProperty ref = (RefProperty) property;
      Model model = definitions.get(ref.getSimpleRef());
      if(model != null) {
        if(model.getExample() != null) {
          return model.getExample();
        }
        Map<String, Object> values = new TreeMap<String, Object>();
        Map<String, Property> properties = model.getProperties();
        for(String key : properties.keySet()) {
          Property innerProp = properties.get(key);
          if(!processedModels.contains(key)) {
            values.put(key, fromProperty(innerProp, definitions, processedModels));
            processedModels.add(key);
          }
          else
            values.put(key, "{}");
        }
        return values;
      }
    }
    return null;
  }
}
