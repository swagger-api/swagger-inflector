/*
 *  Copyright 2017 SmartBear Software
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

package io.swagger.oas.inflector.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.swagger.oas.inflector.Constants;
import io.swagger.oas.inflector.config.Configuration;
import io.swagger.oas.inflector.models.RequestContext;

import io.swagger.oas.models.Operation;
import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.BooleanSchema;
import io.swagger.oas.models.media.ByteArraySchema;
import io.swagger.oas.models.media.DateSchema;
import io.swagger.oas.models.media.DateTimeSchema;
import io.swagger.oas.models.media.EmailSchema;
import io.swagger.oas.models.media.FileSchema;
import io.swagger.oas.models.media.IntegerSchema;
import io.swagger.oas.models.media.MediaType;
import io.swagger.oas.models.media.NumberSchema;
import io.swagger.oas.models.media.ObjectSchema;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.media.StringSchema;
import io.swagger.oas.models.media.UUIDSchema;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;
import io.swagger.util.Json;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class ReflectionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    protected Configuration config;
    protected Set<String> unimplementedMappedModels = new TreeSet<String>();
    private ClassNameValidator classNameValidator = new ClassNameValidator() {
        @Override
        public boolean isValidClassname(String classname) {
            try {
                return Class.forName(classname) != null;
            } catch (ClassNotFoundException e) {
                LOGGER.warn( "Failed to find class [" + classname + "]");
                return false;
            }
        }
    };

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    public Configuration getConfiguration() {
        return config;
    }

    public JavaType[] getOperationParameterClasses(Operation operation, Map<String, Schema> definitions) {
        TypeFactory tf = Json.mapper().getTypeFactory();

        if (operation.getParameters() == null){
            operation.setParameters(new ArrayList<Parameter>());
        }

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

    public JavaType[] getOperationRequestBodyClasses(Operation operation, Map<String, Schema> definitions) {
        TypeFactory tf = Json.mapper().getTypeFactory();

        if (operation.getRequestBody() != null) {

            JavaType[] jt = new JavaType[2];
            int i = 0;
            jt[i] = tf.constructType(RequestContext.class);

            i += 1;

            JavaType argumentClass = getTypeFromRequestBody(operation.getRequestBody(), definitions);
            jt[i] = argumentClass;

            return jt;
        }

        return  null;

    }

    public JavaType getTypeFromRequestBody(RequestBody body,Map<String, Schema> definitions ){

        if (body.getContent() != null) {
            Map<String,MediaType> content   = body.getContent();
            for (String mediaType : content.keySet()){
                if (content.get(mediaType).getSchema() != null) {
                    Schema model = content.get(mediaType).getSchema();
                    return getTypeFromModel("", model, definitions);
                }
            }
        }

        return null;
    }

    public JavaType getTypeFromParameter(Parameter parameter, Map<String, Schema> definitions) {
      if (parameter.getSchema() != null) {
          JavaType parameterType =  getTypeFromModel("", parameter.getSchema(), definitions);
          if (parameterType != null){
              return parameterType;
          }
         }

      else if (parameter.getContent() != null) {
          Map<String,MediaType> content   = parameter.getContent();
          for (String mediaType : content.keySet()){
              if (content.get(mediaType).getSchema() != null) {
                  Schema model = content.get(mediaType).getSchema();
                  return getTypeFromModel("", model, definitions);
              }
          }
      }
      
      return null;
    }




    public JavaType getTypeFromProperty(String type, String format, Schema property, Map<String, Schema> definitions) {
        TypeFactory tf = Json.mapper().getTypeFactory();


        if(property instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema)property;
            Schema inner = arraySchema.getItems();
            JavaType innerType = getTypeFromProperty(null, null, inner, definitions);
            return tf.constructCollectionType(List.class, innerType);
        }
        if(property.getAdditionalProperties() != null) {
            Schema inner = property.getAdditionalProperties();
            JavaType innerType = getTypeFromProperty(null, null, inner, definitions);
            return tf.constructMapLikeType(Map.class, getTypeFromProperty("string", null, null, definitions), innerType);
        }
        if(property.get$ref() != null) {
            if(definitions != null) {
                String ref= property.get$ref();
                ref = ref.substring(ref.lastIndexOf("/") + 1);
                Schema model = definitions.get(ref);
                if(model != null) {
                    JavaType mt = getTypeFromModel(ref, model, definitions);
                    if(mt != null) {
                        return mt;
                    }
                }
            }
        }
        if("array".equals(type)) {
            JavaType innerType = getTypeFromProperty(null, null, property, definitions);
            return tf.constructCollectionType(List.class, innerType);
        }
        if(("byte".equals(type)) || property instanceof ByteArraySchema) {
            return tf.constructType(Byte[].class);
        }
        if(("boolean".equals(type)) || property instanceof BooleanSchema) {
            return tf.constructType(Boolean.class);
        }
        if(("string".equals(type) && "date".equals(format)) || property instanceof DateSchema) {
            return tf.constructType(LocalDate.class);
        }
        if(("string".equals(type) && "date-time".equals(format)) || property instanceof DateTimeSchema) {
          return tf.constructType(DateTime.class);
        }
        if(("string".equals(type) && format == null) || property instanceof StringSchema) {
          return tf.constructType(String.class);
        }
        if(("number".equals(type) && format == null) && property instanceof NumberSchema) {
            return tf.constructType(BigDecimal.class);
        }
        if(("number".equals(type) && "double".equals(format)) && property instanceof NumberSchema) {
            return tf.constructType(Double.class);
        }
        if(("string".equals(type) && "email".equals(format)) || property instanceof EmailSchema) {
            return tf.constructType(String.class);
        }
        if(("number".equals(type) && "float".equals(format)) && property instanceof NumberSchema) {
            return tf.constructType(Float.class);
        }
        if(("string".equals(type) && "uuid".equals(format)) || property instanceof UUIDSchema) {
            return tf.constructType(UUID.class);
        }
        if(("file".equals(type)) || property instanceof FileSchema) {
            return tf.constructType(File.class);
        }
        if(("integer".equals(type) && "int32".equals(format)) && property instanceof IntegerSchema) {
            return tf.constructType(Integer.class);
        }
        if(("integer".equals(type) && "int64".equals(format)) && property instanceof IntegerSchema) {
            return tf.constructType(Long.class);
        }
        if("integer".equals(type)) {
            // fallback
            LOGGER.warn("falling back to `string` with format `" + format + "`");
            return tf.constructType(Long.class);
        }
        if("string".equals(type) || property instanceof StringSchema) {
            // fallback
            LOGGER.warn("falling back to `string` with format `" + format + "`");
            return tf.constructType(String.class);
        }
        if(property instanceof ObjectSchema) {
            final String name = (String) property.getExtensions()
                    .get(Constants.X_SWAGGER_ROUTER_MODEL);
            if (name != null) {
                final JavaType modelType = getTypeFromModelName(name);
                if (modelType != null) {
                    return modelType;
                }
            }
            return tf.constructType(JsonNode.class);
        }
        return null;
    }
    
    public JavaType getTypeFromModel(String name, Schema model, Map<String, Schema> definitions) {
        TypeFactory tf = Json.mapper().getTypeFactory();

        if(model.get$ref() != null && "".equals(name)) {
            String ref= model.get$ref();
            ref = ref.substring(ref.lastIndexOf("/") + 1);
            name = ref;
        }
        if(config != null && config.getModelMapping(name) != null) {
            return tf.constructType(config.getModelMapping(name));
        }

        if(model.getExtensions() != null && model.getExtensions().get(Constants.X_SWAGGER_ROUTER_MODEL) != null) {
            final JavaType modelType = getTypeFromModelName(
                    (String) model.getExtensions().get(Constants.X_SWAGGER_ROUTER_MODEL));
            if (modelType != null) {
                return modelType;
            }
        }
        if(model.get$ref() != null) {
            String ref= model.get$ref();
            ref = ref.substring(ref.lastIndexOf("/") + 1);
            Schema inner = definitions.get(ref);
            if(inner != null) {
                return getTypeFromModel(name, inner, definitions);
            }
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
            if(model.getExtensions() == null || model.getExtensions().get(Constants.X_SWAGGER_ROUTER_MODEL) == null) {
                // add to unimplemented models
                unimplementedMappedModels.add(modelName);
            }
        }
        if(model instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) model;
            Schema inner = arraySchema.getItems();
            if(inner != null) {
                JavaType innerType = getTypeFromProperty(inner.getType(), inner.getFormat(), inner, definitions);
                if (innerType != null) {
                    return tf.constructArrayType(innerType);
                } else {
                    return tf.constructArrayType(JsonNode.class);
                }
            }
        }
        if(model.getProperties() != null) {

            String type = model.getType();

            Schema property = propertyFromModel(model);
            if(property != null) {
                return getTypeFromProperty(model.getType(), model.getFormat(), property, definitions);
            }
        }
        if(model instanceof StringSchema) {

            StringSchema stringSchema = (StringSchema) model;
            JavaType innerType = getTypeFromProperty(stringSchema.getType(), stringSchema.getFormat(), stringSchema, definitions);
            if (innerType != null) {
                return tf.constructType(innerType);
            } else {
                return tf.constructType(JsonNode.class);
            }
        }
        if(model instanceof DateSchema) {

            DateSchema dateSchema = (DateSchema) model;
            JavaType innerType = getTypeFromProperty(dateSchema.getType(), dateSchema.getFormat(), dateSchema, definitions);
            if (innerType != null) {
                return tf.constructType(innerType);
            } else {
                return tf.constructType(JsonNode.class);
            }
        }
        if(model instanceof BooleanSchema) {

            BooleanSchema booleanSchema = (BooleanSchema) model;
            JavaType innerType = getTypeFromProperty(booleanSchema.getType(), booleanSchema.getFormat(), booleanSchema, definitions);
            if (innerType != null) {
                return tf.constructType(innerType);
            } else {
                return tf.constructType(JsonNode.class);
            }
        }
        if(model instanceof DateTimeSchema) {

            DateTimeSchema dateTimeSchema = (DateTimeSchema) model;
            JavaType innerType = getTypeFromProperty(dateTimeSchema.getType(), dateTimeSchema.getFormat(), dateTimeSchema, definitions);
            if (innerType != null) {
                return tf.constructType(innerType);
            } else {
                return tf.constructType(JsonNode.class);
            }
        }
        if(model instanceof IntegerSchema) {

            IntegerSchema integerSchema = (IntegerSchema) model;
            JavaType innerType = getTypeFromProperty(integerSchema.getType(), integerSchema.getFormat(), integerSchema, definitions);
            if (innerType != null) {
                return tf.constructType(innerType);
            } else {
                return tf.constructType(JsonNode.class);
            }
        }
        if(model instanceof NumberSchema) {

            NumberSchema numberSchema = (NumberSchema) model;
            JavaType innerType = getTypeFromProperty(numberSchema.getType(), numberSchema.getFormat(), numberSchema, definitions);
            if (innerType != null) {
                return tf.constructType(innerType);
            } else {
                return tf.constructType(JsonNode.class);
            }
        }else {
            Schema property = propertyFromModel(model);
            if(property != null) {
                return getTypeFromProperty(model.getType(), model.getFormat(), property, definitions);
            }
        }
        return tf.constructType(JsonNode.class);
    }

    public Schema propertyFromModel(Schema model) {
        if(model.getType() == null) {
            return null;
        }
        Schema property = new Schema();
        if(model.getType() != null){
            property.setType(model.getType());
        }
        if(model.getTitle() != null){
            property.setTitle(model.getTitle());
        }
        if(model.getDescription() != null) {
            property.setDescription(model.getDescription());
        }
        if(model.getDefault() != null){
             property.setDefault(model.getDefault());
        }
        if(model.getExample() != null){
            property.setExample(model.getExample());
        }
        if(model.getFormat() != null) {
            property.setFormat(model.getFormat());
        }
        if(model.getExtensions() != null) {
             property.setExample(model.getExtensions());
        }


        return property;
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
        String name = null;
        if (operation.getExtensions() != null){
            name = (String) operation.getExtensions().get(Constants.X_SWAGGER_ROUTER_CONTROLLER);
        }
        if (name != null) {
            name = name.replaceAll("^\"|\"$", "");
            if (name.indexOf(".") == -1 && config.getControllerPackage() != null) {
                name = config.getControllerPackage() + "." + name;
            }

            if( classNameValidator.isValidClassname( name )) {
                return name;
            }
        }

        if (operation.getTags() != null && operation.getTags().size() > 0) {

            for( String tag : operation.getTags()){
                name = StringUtils.capitalize(sanitizeToJava(tag));
                if (config.getControllerPackage() != null) {
                    name = config.getControllerPackage() + "." + name;
                }

                if( classNameValidator.isValidClassname( name )){
                    return name;
                }
                else if( classNameValidator.isValidClassname( name + "Controller" )){
                    return name + "Controller";
                }
                else if( classNameValidator.isValidClassname( sanitizeToJava("default"))) {
                    return sanitizeToJava("default");
                }
                else if( classNameValidator.isValidClassname( sanitizeToJava("default"))) {
                    return sanitizeToJava("default") + "Controller";
                }
            }
        }

        String controllerClass = config.getControllerClass();
        if( StringUtils.isEmpty( controllerClass )){
            controllerClass = StringUtils.capitalize(sanitizeToJava("default"));
        }

        return config.getControllerPackage() + "." + controllerClass;
    }

    public Set<String> getUnimplementedMappedModels() {
        return unimplementedMappedModels;
    }

    public void setUnimplementedMappedModels(Set<String> unimplementedMappedModels) {
        this.unimplementedMappedModels = unimplementedMappedModels;
    }

    public ClassNameValidator getClassNameValidator() {
        return classNameValidator;
    }

    public void setClassNameValidator(ClassNameValidator classNameValidator) {
        this.classNameValidator = classNameValidator;
    }

    private JavaType getTypeFromModelName(String name) {
        final TypeFactory tf = Json.mapper().getTypeFactory();
        // it's legal to have quotes around the model name so trim them
        String modelName = name.replaceAll("^\"|\"$", "");
        Class<?> cls = loadClass(modelName);
        if(cls != null) {
            return tf.constructType(cls);
        }
        if(config.getModelPackage() != null && !modelName.contains(".")) {
            modelName = config.getModelPackage() + "." + modelName;
            cls = loadClass(modelName);
            if (cls != null) {
                return tf.constructType(cls);
            }
        }
        unimplementedMappedModels.add(modelName);
        return null;
    }

    public interface ClassNameValidator {
        boolean isValidClassname( String classname );
    }
}