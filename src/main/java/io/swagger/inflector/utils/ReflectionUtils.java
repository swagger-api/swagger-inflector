package io.swagger.inflector.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.Null;

import io.swagger.inflector.config.*;
import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ReflectionUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

  protected Configuration config;

  public void setConfiguration(Configuration config) {
    this.config = config;
  }
  
  public Class<?>[] getOperationParameterClasses(Operation operation, Map<String, Model> definitions) {
    Class<?>[] classes = new Class<?>[operation.getParameters().size()];
    int i = 0;
    for(Parameter parameter : operation.getParameters()) {
      Class<?> argumentClass = getParameterSignature(parameter, definitions);
      classes[i] = argumentClass;
      i += 1;
    }
    return classes;
  }

  public Class<?> getParameterSignature(Parameter parameter, Map<String, Model> definitions) {
    if(parameter instanceof SerializableParameter) {
      SerializableParameter sp = (SerializableParameter) parameter;
      String type = sp.getType();
      String format = sp.getFormat();
      switch (type) {
      case "string":
        if("date".equals(format)) {
          return LocalDate.class;
        }
        else if("date-time".equals(format)) {
          return DateTime.class;
        }
        else if("uuid".equals(format)) {
          return UUID.class;
        }
        return String.class;
      case "integer":
        if("int32".equals(format)) {
          return Integer.class;
        }
        else if("int64".equals(format)) {
          return Long.class;
        }
        break;
      case "number":
        if("float".equals(format)) {
          return Float.class;
        }
        else if("double".equals(format)) {
          return Double.class;
        }
        return BigDecimal.class;
      case "boolean":
        return Boolean.class;
      case "array":
        return List.class;
      case "file":
        throw new RuntimeException("not implemented");
      }
      LOGGER.error("oops! Couldn't match " + type + ", " + format);
    }
    else if (parameter instanceof BodyParameter) {
      BodyParameter body = (BodyParameter) parameter;
      Model model = body.getSchema();
      if(model instanceof RefModel) {
        RefModel ref = (RefModel) model;
        Model referencedModel = definitions.get(ref.getSimpleRef());
        return detectModel(ref.getSimpleRef(), referencedModel);
      }
      else if(model instanceof ArrayModel) {
        return List.class;
      }
    }
    else {
      throw new RuntimeException("not implemented! " + parameter);
    }
    return Null.class;
  }

  public Class<?> detectModel(String name, Model model) {
    // TODO reference github issue for this
    // there are no vendor extensions in the Model!  This makes it hard...
    Class<?> output = config.getModelMapping(name);
    if(output != null) {
      // found a mapping in the configuration
      LOGGER.debug("found model in config mapping: " + output);
      return output;
    }
    // try to look up by name
    try {
      return Class.forName(name);
    }
    catch (ClassNotFoundException e) {
      // continue
    }
    // try with config prefix
    if(config.getModelPackage() != null && name.indexOf(".") == -1) {
      String fqModel = config.getModelPackage() + "." + name;
      try {
        return Class.forName(fqModel);
      }
      catch (ClassNotFoundException e) {
        // continue
      }
    }
    LOGGER.debug("model `" + name + "` not found in classloader");

    return JsonNode.class;
  }
  
  // TODO move to core
  public String getMethodName(String path, String httpMethod, Operation operation) {
    String output = operation.getOperationId();
    if(output != null) {
      return output;
    }
    String tmpPath = path;
    tmpPath = tmpPath.replaceAll("\\{", "");
    tmpPath = tmpPath.replaceAll("\\}", "");
    String[] parts = (tmpPath + "/" + httpMethod).split("/");
    StringBuilder builder = new StringBuilder();
    if ("/".equals(tmpPath)) {
      // must be root tmpPath
      builder.append("root");
    }
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      if (part.length() > 0) {
        if (builder.toString().length() == 0) {
          part = Character.toLowerCase(part.charAt(0)) + part.substring(1);
        } else {
          part = StringUtils.capitalize(part);
        }
        builder.append(part);
      }
    }
    output = builder.toString();
    LOGGER.warn("generated operationId " + output);
    return output;
  }

  public String getControllerName(Operation operation) {
    String name = (String)operation.getVendorExtensions().get("x-swagger-router-controller");
    if(name != null) {
      if(name.indexOf(".") == -1 && config.getControllerPackage() != null) {
        return config.getControllerPackage() + "." + name;
      }
      else {
        return name;
      }
    }
    if(operation.getTags() != null && operation.getTags().size() > 0) {
      return operation.getTags().get(0);
    }
    return null;
  }

  public Object cast(List<String> o, Parameter parameter, Class<?> cls, Map<String, Model> definitions) {
    if(o == null || o.size() == 0)
      return null;

    LOGGER.debug("coercing array `" + o + "` to `" + cls + "`");
    if(List.class.equals(cls)) {
      if(parameter instanceof SerializableParameter) {
        List<Object> output = new ArrayList<Object>();
        SerializableParameter sp = (SerializableParameter) parameter;
        if(sp.getItems() != null) {
          if(sp.getItems()instanceof ArrayProperty) {
            Property inner = ((ArrayProperty) sp.getItems()).getItems();

            // TODO: this does not need to be done this way, update the helper method
            Parameter innerParam = new QueryParameter().property(inner);
            Class<?> innerClass = getParameterSignature(innerParam, definitions);
            for(String obj : o) {
              String[] parts = new String[0];
              CSVFormat format = null;
              if("csv".equals(sp.getCollectionFormat()) && !StringUtils.isEmpty(obj)) {
                format = CSVFormat.DEFAULT;
              }
              else if("pipes".equals(sp.getCollectionFormat()) && !StringUtils.isEmpty(obj)) {
                format = CSVFormat.newFormat('|').withQuote('"');
              }
              else if("ssv".equals(sp.getCollectionFormat()) && !StringUtils.isEmpty(obj)) {
                format = CSVFormat.newFormat(' ').withQuote('"');
              }
              if(format != null) {
                try {
                  for(CSVRecord record : CSVParser.parse(obj, format).getRecords()) {
                    List<String> it = new ArrayList<String>();
                    for(Iterator<String> x = record.iterator(); x.hasNext();) {
                      it.add(x.next());
                    }
                    parts = it.toArray(new String[it.size()]);
                  }
                } catch (IOException e) {}
              }
              else {
                parts = new String[1];
                parts[0] = obj;
              }
              for(String p : parts) {
                Object ob = cast(p, inner, innerClass);
                if(ob != null) {
                  output.add(ob);
                }
              }
            }
          }
        }
        return output;
      }
    }
    else if(parameter instanceof SerializableParameter) {
      SerializableParameter sp = (SerializableParameter) parameter;
      return cast(o.get(0), sp.getItems(), cls);
    }
    return null;
  }

  public Object cast(String o, Property property, Class<?> cls) {
    if(o == null)
      return null;

    LOGGER.debug("coercing `" + o + "` to `" + cls + "`");
    try {
      if(Integer.class.equals(cls)) {
        return Integer.parseInt(o);
      }
      if(Long.class.equals(cls)) {
        return Long.parseLong(o);
      }
      if(Float.class.equals(cls)) {
        return Float.parseFloat(o);
      }
      if(Double.class.equals(cls)) {
        return Double.parseDouble(o);
      }
      if(String.class.equals(cls)) {
        return o;
      }
      if(Boolean.class.equals(cls)) {
        if("1".equals(o))
          return Boolean.TRUE;
        if("0".equals(o))
          return Boolean.FALSE;
        return Boolean.parseBoolean(o);
      }
      if(UUID.class.equals(cls)) {
        return UUID.fromString(o);
      }
    }
    catch (NumberFormatException e) {
      LOGGER.debug("couldn't coerce `" + o + "` to type " + cls);
    }
    catch (IllegalArgumentException e) {
      LOGGER.debug("couldn't coerce `" + o + "` to type " + cls);
    }
    return null;
  }
}