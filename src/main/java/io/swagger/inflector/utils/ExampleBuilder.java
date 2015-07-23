package io.swagger.inflector.utils;

import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.EmailProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.UUIDProperty;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ExampleBuilder {
  public static Object fromProperty(Property property, Map<String, Model> definitions) {
    return fromProperty(property, definitions, new HashSet<String>());
  }
  public static Object fromProperty(Property property, Map<String, Model> definitions, Set<String> processedModels) {
    if(property == null) {
      return null;
    }

    Object example = property.getExample();
    if(property instanceof RefProperty) {
      RefProperty ref = (RefProperty) property;
      Model model = definitions.get(ref.getSimpleRef());
      if(model != null) {
        if(model.getExample() != null) {
          return model.getExample();
        }
      }
    }
    if(property instanceof EmailProperty) {
      if(example !=  null) {
        return example;
      }
      return "apiteam@swagger.io";
    }
    if(property instanceof UUIDProperty) {
      if(example != null) {
        return example.toString();
      }
      return "3fa85f64-5717-4562-b3fc-2c963f66afa6";
    }
    if(property instanceof StringProperty) {
      if(example != null) {
        return example;
      }
      return "string";
    }
    if(property instanceof IntegerProperty) {
      if(example != null) {
        return Integer.parseInt(example.toString());
      }
      return new Integer(0);
    }
    if(property instanceof LongProperty) {
      if(example != null) {
        return Long.parseLong(example.toString());
      }
      return new Long(0);
    }
    if(property instanceof FloatProperty) {
      if(example != null) {
        return Float.parseFloat(example.toString());
      }
      return new Float(1.1);
    }
    if(property instanceof DoubleProperty) {
      if(example != null) {
        return Double.parseDouble(example.toString());
      }
      return new Double(1.23);
    }
    if(property instanceof BooleanProperty) {
      if(example != null) {
        return Boolean.parseBoolean(example.toString());
      }
      return Boolean.TRUE;
    }
    if(property instanceof DateProperty) {
      if(example != null) {
        return example;
      }
      return "2015-07-20";
    }
    if(property instanceof DateTimeProperty) {
      if(example != null) {
        return example;
      }
      return "2015-07-20T15:49:04-07:00";
    }
    if(property instanceof DecimalProperty) {
      if(example != null) {
        return new BigDecimal(example.toString());
      }
      return new BigDecimal(1.5);
    }
    if(property instanceof ObjectProperty) {
      if(example != null) {
        return example;
      }
      return "{}";
    }
    if(property instanceof ArrayProperty) {
      ArrayProperty ap = (ArrayProperty) property;
      Property inner = ap.getItems();
      if(inner != null) {
        Object[] o = new Object[1];
        Object innerExample = fromProperty(inner, definitions, processedModels);
        if(innerExample != null) {
          if(innerExample instanceof String) {
            return "[" + innerExample + "]";
          }
          o[0] = innerExample;
          return o;
        }
      }
    }
    if(property instanceof MapProperty) {
      MapProperty mp = (MapProperty) property;
      Property inner = mp.getAdditionalProperties();
      if(inner != null) {
        Object innerExample = fromProperty(inner, definitions, processedModels);
        if(innerExample != null) {
          if(innerExample instanceof String) {
            return "{\"key\" : " + innerExample + "}";
          }
          Map<String, Object> outputMap = new HashMap<String, Object>();
          outputMap.put("key", innerExample);
          return outputMap;
        }
      }
    }

    // TODO: File, Map
    
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
