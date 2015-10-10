/*
 *  Copyright 2015 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.swagger.inflector.utils;

import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.models.RequestContext;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;

import io.swagger.util.Json;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ReflectionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    protected Configuration config;
    protected Set<String> unimplementedMappedModels = new TreeSet<String>();

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    public JavaType[] getOperationParameterClasses(Operation operation, Map<String, Model> definitions) {
        TypeFactory tf = Json.mapper().getTypeFactory();

        JavaType[] jt = new JavaType[operation.getParameters().size() + 1];
        int i = 0;
        jt[i] = tf.constructType(RequestContext.class);

        i += 1;

        for (Parameter parameter : operation.getParameters()) {
            JavaType argumentClass = getTypeFromParameter(parameter, definitions);
            jt[i] = argumentClass;
            i += 1;
        }
        return jt;
    }

    public JavaType getTypeFromParameter(Parameter parameter, Map<String, Model> definitions) {
      if (parameter instanceof SerializableParameter) {
          SerializableParameter sp = (SerializableParameter) parameter;
          Property inner = sp.getItems();
          
          JavaType tp = getTypeFromProperty(sp.getType(), sp.getFormat(), inner, definitions);
          if(tp != null) {
              return tp;
          }
      }
      else if (parameter instanceof BodyParameter) {
          BodyParameter bp = (BodyParameter) parameter;
          Model model = bp.getSchema();

          return getTypeFromModel("", model, definitions);
      }
      
      return null;
    }

    public JavaType getTypeFromProperty(String type, String format, Property property, Map<String, Model> definitions) {
        TypeFactory tf = Json.mapper().getTypeFactory();

        if(("byte".equals(type)) || property instanceof ByteArrayProperty) {
            return tf.constructType(Byte[].class);
        }
        if(("boolean".equals(type)) || property instanceof BooleanProperty) {
            return tf.constructType(Boolean.class);
        }
        if(("string".equals(type) && "date".equals(format)) || property instanceof DateProperty) {
            return tf.constructType(LocalDate.class);
        }
        if(("string".equals(type) && "date-time".equals(format)) || property instanceof DateTimeProperty) {
          return tf.constructType(DateTime.class);
        }
        if(("string".equals(type) && format == null) || property instanceof StringProperty) {
          return tf.constructType(String.class);
        }
        if(("number".equals(type) && format == null) || property instanceof DecimalProperty) {
            return tf.constructType(BigDecimal.class);
        }
        if(("number".equals(type) && "double".equals(format)) || property instanceof DoubleProperty) {
            return tf.constructType(Double.class);
        }
        if(("string".equals(type) && "email".equals(format)) || property instanceof EmailProperty) {
            return tf.constructType(String.class);
        }
        if(("number".equals(type) && "float".equals(format)) || property instanceof FloatProperty) {
            return tf.constructType(Float.class);
        }
        if(("string".equals(type) && "uuid".equals(format)) || property instanceof UUIDProperty) {
            return tf.constructType(UUID.class);
        }
        if(("file".equals(type)) || property instanceof FileProperty) {
            return tf.constructType(File.class);
        }
        if(("integer".equals(type) && "int32".equals(format)) || property instanceof IntegerProperty) {
            return tf.constructType(Integer.class);
        }
        if(("integer".equals(type) && "int64".equals(format)) || property instanceof LongProperty) {
            return tf.constructType(Long.class);
        }
        if("integer".equals(type)) {
            // fallback
            LOGGER.warn("falling back to `string` with format `" + format + "`");
            return tf.constructType(Long.class);
        }
        if("string".equals(type) || property instanceof StringProperty) {
            // fallback
            LOGGER.warn("falling back to `string` with format `" + format + "`");
            return tf.constructType(String.class);
        }
        if(property instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty)property;
            Property inner = ap.getItems();
            JavaType innerType = getTypeFromProperty(null, null, inner, definitions);
            return tf.constructArrayType(innerType);
        }
        if(property instanceof MapProperty) {
            MapProperty mp = (MapProperty) property;
            Property inner = mp.getAdditionalProperties();
            JavaType innerType = getTypeFromProperty(null, null, inner, definitions);
            return tf.constructMapLikeType(Map.class, getTypeFromProperty("string", null, null, definitions), innerType);
        }
        if(property instanceof RefProperty) {
            RefProperty ref = (RefProperty) property;
            if(definitions != null) {
                Model model = definitions.get(ref.getSimpleRef());
                if(model != null) {
                    JavaType mt = getTypeFromModel(ref.getSimpleRef(), model, definitions);
                    if(mt != null) {
                        return mt;
                    }
                }
            }
        }
        return null;
    }
    
    public JavaType getTypeFromModel(String name, Model model, Map<String, Model> definitions) {
        TypeFactory tf = Json.mapper().getTypeFactory();

        if(model instanceof RefModel && "".equals(name)) {
            RefModel ref = (RefModel) model;
            name = ref.getSimpleRef();
        }
        if(config != null && config.getModelMapping(name) != null) {
            return tf.constructType(config.getModelMapping(name));
        }

        if(model.getVendorExtensions() != null && model.getVendorExtensions().get("x-swagger-router-model") != null) {
            String modelName = model.getVendorExtensions().get("x-swagger-router-model").toString();
            // it's legal to have quotes around the model name so trim them
            modelName = modelName.replaceAll("^\"|\"$", "");
            Class<?> cls = loadClass(modelName);
            if(cls != null) {
                return tf.constructType(cls);
            }            
            if(config.getModelPackage() != null && modelName.indexOf(".") == -1) {
                modelName = config.getModelPackage() + "." + modelName;
            }
            cls = loadClass(modelName);
            if(cls != null) {
                return tf.constructType(cls);
            }
            unimplementedMappedModels.add(modelName);
        }
        // try to load from default package
        if(!"".equals(name)) {
            String modelName = name;
            if(config.getModelPackage() != null && name.indexOf(".") == -1) {
                modelName = config.getModelPackage() + "." + modelName;
            }
            Class<?> cls = loadClass(modelName);
            if(cls != null) {
                return tf.constructType(cls);
            }
            // check to avoid double-counting
            if(model.getVendorExtensions() == null || model.getVendorExtensions().get("x-swagger-router-model") == null) {
                // add to unimplemented models
                unimplementedMappedModels.add(modelName);
            }
        }
        if(model instanceof ArrayModel) {
            ArrayModel am = (ArrayModel) model;
            Property inner = am.getItems();
            JavaType innerType = getTypeFromProperty(inner.getType(), inner.getFormat(), inner, definitions);
            if(innerType != null) {
                return tf.constructArrayType(innerType);
            }
            else {
                return tf.constructArrayType(JsonNode.class);
            }
        }
        if(model instanceof RefModel) {
            RefModel ref = (RefModel) model;
            Model inner = definitions.get(ref.getSimpleRef());
            if(inner != null) {
                return getTypeFromModel(name, inner, definitions);
            }
        }
        return tf.constructType(JsonNode.class);
    }
    
    public Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    public String sanitizeToJava(String operationId) {
        String op = operationId.trim();
        op = op.replaceAll("[^a-zA-Z0-9]", "_");
        if(op.length() == 0) {
            return "nullId";
        }
        return op;
    }

    // TODO move to core
    public String getMethodName(String path, String httpMethod, Operation operation) {
        String output = operation.getOperationId();
        if (output != null) {
            return sanitizeToJava(output);
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
        String name = (String) operation.getVendorExtensions().get("x-swagger-router-controller");
        if (name != null) {
            name = name.replaceAll("^\"|\"$", "");
            if (name.indexOf(".") == -1 && config.getControllerPackage() != null) {
                return config.getControllerPackage() + "." + name;
            } else {
                return name;
            }
        }
        if (operation.getTags() != null && operation.getTags().size() > 0) {
            String className = StringUtils.capitalize(sanitizeToJava(operation.getTags().get(0)));
            if (config.getControllerPackage() != null) {
                return config.getControllerPackage() + "." + className;
            }
            return className;
        }
        return null;
    }

    public Set<String> getUnimplementedMappedModels() {
        return unimplementedMappedModels;
    }

    public void setUnimplementedMappedModels(Set<String> unimplementedMappedModels) {
        this.unimplementedMappedModels = unimplementedMappedModels;
    }
}