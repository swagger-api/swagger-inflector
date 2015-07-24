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
import io.swagger.util.Json;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

public class ExampleBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleBuilder.class);

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
          try {
            return Json.mapper().readValue(model.getExample().toString(), JsonNode.class);
          } catch (IOException e) {
            return null;
          }
        }
      }
    }
    if(property instanceof EmailProperty) {
      if(example !=  null) {
        return example;
      }
      return new TextNode("apiteam@swagger.io");
    }
    if(property instanceof UUIDProperty) {
      if(example != null) {
        return new TextNode(example.toString());
      }
      return new TextNode("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    }
    if(property instanceof StringProperty) {
      if(example != null) {
        return new TextNode(example.toString());
      }
      return new TextNode("string");
    }
    if(property instanceof IntegerProperty) {
      if(example != null) {
        return new IntNode(Integer.parseInt(example.toString()));
      }
      return new IntNode(0);
    }
    if(property instanceof LongProperty) {
      if(example != null) {
        return new LongNode(Long.parseLong(example.toString()));
      }
      return new LongNode(0);
    }
    if(property instanceof FloatProperty) {
      if(example != null) {
        return new FloatNode(Float.parseFloat(example.toString()));
      }
      return new FloatNode(1.1f);
    }
    if(property instanceof DoubleProperty) {
      if(example != null) {
        return new DoubleNode(Double.parseDouble(example.toString()));
      }
      return new DoubleNode(1.23);
    }
    if(property instanceof BooleanProperty) {
      if(example != null) {
        return BooleanNode.valueOf(Boolean.parseBoolean(example.toString()));
      }
      return BooleanNode.valueOf(true);
    }
    if(property instanceof DateProperty) {
      if(example != null) {
        return new TextNode(example.toString());
      }
      return new TextNode("2015-07-20");
    }
    if(property instanceof DateTimeProperty) {
      if(example != null) {
        return new TextNode(example.toString());
      }
      return new TextNode("2015-07-20T15:49:04-07:00");
    }
    if(property instanceof DecimalProperty) {
      if(example != null) {
        return new DecimalNode(new BigDecimal(example.toString()));
      }
      return new DecimalNode(new BigDecimal(1.5));
    }
    if(property instanceof ObjectProperty) {
      if(example != null) {
        try {
          return Json.mapper().readValue(example.toString(), JsonNode.class);
        } catch (IOException e) {
          LOGGER.error("unable to convert `" + example + "` to JsonNode");
          return null;
        }
      }
      return Json.mapper().createObjectNode();
    }
    if(property instanceof ArrayProperty) {
      ArrayProperty ap = (ArrayProperty) property;
      Property inner = ap.getItems();
      if(inner != null) {
        Object innerExample = fromProperty(inner, definitions, processedModels);
        if(innerExample != null) {
          if(innerExample instanceof JsonNode) {
            ArrayNode an = Json.mapper().createArrayNode();
            an.add((JsonNode) innerExample);
            return an;
          }
          Object[] o = new Object[1];

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
          ObjectNode on = Json.mapper().createObjectNode();

          if(innerExample instanceof JsonNode) {
            on.put("key", (JsonNode) innerExample);
            return on;
          }
          Map<String, Object> outputMap = new HashMap<String, Object>();
          outputMap.put("key", innerExample);
          return outputMap;
        }
      }
    }

    // TODO: File
    if(property instanceof RefProperty) {
      RefProperty ref = (RefProperty) property;
      Model model = definitions.get(ref.getSimpleRef());
      if(model != null) {
        if(model.getExample() != null) {
          try {
            JsonNode n = Json.mapper().readValue(model.getExample().toString(), JsonNode.class);
            return n;
          } catch (IOException e) {
            return null;
          }
        }
        ObjectNode values = Json.mapper().createObjectNode();
        
        Map<String, Property> properties = model.getProperties();
        for(String key : properties.keySet()) {
          Property innerProp = properties.get(key);
            values.put(key, (JsonNode)fromProperty(innerProp, definitions, processedModels));
            processedModels.add(key);
        }
        return values;
      }
    }
    return null;
  }
}
