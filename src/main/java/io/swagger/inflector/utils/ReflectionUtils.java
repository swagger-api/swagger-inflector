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
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.Null;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
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
        Class<?>[] classes = new Class<?>[operation.getParameters().size() + 1];
        int i = 0;
        classes[i] = RequestContext.class;

        i += 1;

        for (Parameter parameter : operation.getParameters()) {
            Class<?> argumentClass = getParameterSignature(parameter, definitions);
            classes[i] = argumentClass;
            i += 1;
        }
        return classes;
    }

    public Class<?> getParameterSignature(Parameter parameter, Map<String, Model> definitions) {
        if (parameter instanceof SerializableParameter) {
            SerializableParameter sp = (SerializableParameter) parameter;
            String type = sp.getType();
            String format = sp.getFormat();

            switch (type) {
                case "string":
                    if ("date".equals(format)) {
                        return LocalDate.class;
                    } else if ("date-time".equals(format)) {
                        return DateTime.class;
                    } else if ("uuid".equals(format)) {
                        return UUID.class;
                    }
                    return String.class;
                case "integer":
                    if ("int32".equals(format)) {
                        return Integer.class;
                    } else if ("int64".equals(format)) {
                        return Long.class;
                    }
                    break;
                case "number":
                    if ("float".equals(format)) {
                        return Float.class;
                    } else if ("double".equals(format)) {
                        return Double.class;
                    }
                    return BigDecimal.class;
                case "boolean":
                    return Boolean.class;
                case "array":
                    return List.class;
                case "file":
                    return InputStream.class;
            }
            LOGGER.error("oops! Couldn't match " + type + ", " + format);
        } else if (parameter instanceof BodyParameter) {
            BodyParameter body = (BodyParameter) parameter;
            Model model = body.getSchema();
            if (model instanceof RefModel) {
                RefModel ref = (RefModel) model;
                Model referencedModel = definitions.get(ref.getSimpleRef());
                return detectModel(ref.getSimpleRef(), referencedModel);
            } else if (model instanceof ArrayModel) {
                return List.class;
            }
        } else {
            throw new RuntimeException("not implemented! " + parameter.getClass());
        }
        return Null.class;
    }

    public Class<?> detectModel(String name, Model model) {
        // TODO reference github issue for this
        // there are no vendor extensions in the Model!  This makes it hard...
        Class<?> output = config.getModelMapping(name);
        if (output != null) {
            // found a mapping in the configuration
            LOGGER.debug("found model in config mapping: " + output);
            return output;
        }
        // try to look up by name
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            // continue
        }
        // try with config prefix
        if (config.getModelPackage() != null && name.indexOf(".") == -1) {
            String fqModel = config.getModelPackage() + "." + name;
            try {
                return Class.forName(fqModel);
            } catch (ClassNotFoundException e) {
                // continue
            }
        }
        LOGGER.debug("model `" + name + "` not found in classloader");

        return JsonNode.class;
    }

    public String sanitizeToJava(String operationId) {
        String op = operationId.trim();
        op = op.replaceAll("[^a-zA-Z]", "_");
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
}