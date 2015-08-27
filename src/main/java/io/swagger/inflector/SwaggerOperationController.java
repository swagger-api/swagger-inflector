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

package io.swagger.inflector;

import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.converters.ConversionException;
import io.swagger.inflector.converters.InputConverter;
import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.models.ApiError;
import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import io.swagger.inflector.processors.EntityProcessorFactory;
import io.swagger.inflector.utils.ApiException;
import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.inflector.validators.ValidationException;
import io.swagger.inflector.validators.ValidationMessage;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.util.Json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;

public class SwaggerOperationController extends ReflectionUtils implements Inflector<ContainerRequestContext, Response> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerOperationController.class);
    
    private static Set<String> commonHeaders = new HashSet<String>();

    static {
      commonHeaders.add("Host");
      commonHeaders.add("User-Agent");
      commonHeaders.add("Accept");
      commonHeaders.add("Content-Type");
      commonHeaders.add("Content-Length");
    }
    
    private String path;
    private String httpMethod;
    private Operation operation;
    private Object controller = null;
    private Method method = null;
    private JavaType[] parameterClasses = null;
    private Map<String, Model> definitions;
    private InputConverter validator;

    public SwaggerOperationController(Configuration config, String path, String httpMethod, Operation operation, Map<String, Model> definitions) {
        this.setConfiguration(config);
        this.path = path;
        this.httpMethod = httpMethod;
        this.operation = operation;
        this.definitions = definitions;

        this.validator = InputConverter.getInstance();

        JavaType[] args = getOperationParameterClasses(operation, definitions);
        StringBuilder builder = new StringBuilder();

        builder.append(getMethodName(path, httpMethod, operation))
                .append("(");

        for (int i = 0; i < args.length; i++) {
            if (i == 0) {
                builder.append(RequestContext.class.getCanonicalName() + " request");
            } else {
                builder.append(", ");
                if(args[i] == null) {
                  LOGGER.error("didn't expect a null class for " + operation.getParameters().get(i - 1).getName());
                }
                else if(args[i].getRawClass() != null) {
                  builder.append(args[i].getRawClass().toString());
                  builder.append(" ").append(operation.getParameters().get(i - 1).getName());
                }
            }
        }
        builder.append(")");

        LOGGER.debug("looking for operation: " + builder.toString());

        this.method = detectMethod(operation);
        if (method == null) {
            this.parameterClasses = args;
            LOGGER.debug("no method to map to, using mock response");
        }
    }

    public Method detectMethod(Operation operation) {
        String controller = getControllerName(operation);
        String methodName = getMethodName(path, httpMethod, operation);

        if (controller != null && methodName != null) {
            try {
                Class<?> cls;
                try {
                    cls = Class.forName(controller);
                } catch (ClassNotFoundException e) {
                    controller = controller + "Controller";
                    cls = Class.forName(controller);
                }

                JavaType[] args = getOperationParameterClasses(operation, this.definitions);
                Method[] methods = cls.getMethods();
                for (Method method : methods) {
                    if (methodName.equals(method.getName())) {
                        Class<?>[] methodArgs = method.getParameterTypes();
                        if (methodArgs.length == args.length) {
                            int i = 0;
                            boolean matched = true;
                            if (!args[i].getRawClass().equals(methodArgs[i])) {
                                LOGGER.debug("failed to match " + args[i] + ", " + methodArgs[i]);
                                matched = false;
                            }
                            if (matched) {
                                this.parameterClasses = args;
                                this.controller = cls.newInstance();
                                LOGGER.debug("matched " + method);
                                return method;
                            }
                        }
                    }
                }

                LOGGER.debug("no match in " + controller);
            } catch (ClassNotFoundException e) {
                LOGGER.debug("didn't find class " + controller);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Response apply(ContainerRequestContext ctx) {
        List<Parameter> parameters = operation.getParameters();
        Object[] args = new Object[parameters.size() + 1];
        if (parameters != null) {
            int i = 0;

            args[i] = new RequestContext()
                .headers(ctx.getHeaders())
                .mediaType(ctx.getMediaType())
                .acceptableMediaTypes(ctx.getAcceptableMediaTypes());
            i += 1;
            List<ValidationMessage> missingParams = new ArrayList<ValidationMessage>();
            UriInfo uri = ctx.getUriInfo();
            String formDataString = null;
            String[] parts = null;
            Set<String> existingKeys = new HashSet<String>();
            
            for(Iterator<String> x = uri.getQueryParameters().keySet().iterator(); x.hasNext(); ) {
              existingKeys.add(x.next() + ": qp");
            }
            for(Iterator<String> x = uri.getPathParameters().keySet().iterator(); x.hasNext(); ) {
              existingKeys.add(x.next() + ": pp");
            }
            for(Iterator<String> x = ctx.getHeaders().keySet().iterator(); x.hasNext(); ) {
              String key = x.next();
//              if(!commonHeaders.contains(key))
//                existingKeys.add(key);
            }
            for(Parameter p : parameters) {
              if(p instanceof FormParameter) {
                if (formDataString == null) {
                  // can only read stream once
                  try {
                    formDataString = IOUtils.toString(ctx.getEntityStream(), "UTF-8");
                    parts = formDataString.split("&");
  
                    for (String part : parts) {
                        String[] kv = part.split("=");
                        existingKeys.add(kv[0] + ": fp");
                    }
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }
              }
            }
/*
            // TODO handling for multipart
            if(ctx.getMediaType().isCompatible(MediaType.MULTIPART_FORM_DATA_TYPE)) {
              MultiPart mp = new MultiPart();
              mp.bodyPart(ctx.getEntityStream(), MediaType.MULTIPART_FORM_DATA_TYPE);

              for(BodyPart bp : mp.getBodyParts()) {
                StreamDataBodyPart sd = new StreamDataBodyPart();
                sd.setStreamEntity(ctx.getEntityStream(), MediaType.TEXT_PLAIN_TYPE);
              }
              System.out.println("bp: " + mp.getBodyParts());
            }
*/
            for (Parameter parameter : parameters) {
                String in = parameter.getIn();
                Object o = null;

                try {
                    if ("formData".equals(in)) {
                        SerializableParameter sp = (SerializableParameter) parameter;
                        if ("file".equals(sp.getType())) {
                            o = ctx.getEntityStream();
                        } else {
                            if (formDataString != null) {
                                for (String part : parts) {
                                    String[] kv = part.split("=");
                                    if(kv != null) {
                                      if(kv.length > 0) {
                                        existingKeys.remove(kv[0] + ": fp");
                                      }
                                      if (kv.length == 2) {
                                          // TODO how to handle arrays here?
                                          String key = kv[0];
                                          String value = kv[1];
                                          if (parameter.getName().equals(key)) {
                                              JavaType jt = parameterClasses[i];
                                              Class<?> cls = jt.getRawClass();
                                              try {
                                                  o = validator.convertAndValidate(Arrays.asList(value), parameter, cls, definitions);
                                              }
                                              catch (ConversionException e) {
                                                  missingParams.add(e.getError());
                                              }
                                              catch (ValidationException e) {
                                                  missingParams.add(e.getValidationMessage());
                                              }
                                          }
                                      }
                                   }
                                }
                            }
                        }
                    }
                    else {
                        try {
                            String paramName = parameter.getName();
                            if("query".equals(in)) {
                              existingKeys.remove(paramName + ": qp");
                            }
                            if("path".equals(in)) {
                              existingKeys.remove(paramName + ": pp");
                            }
                            JavaType jt = parameterClasses[i];
                            Class<?> cls = jt.getRawClass();
                            if ("body".equals(in)) {
                                if (ctx.hasEntity()) {
                                    o = EntityProcessorFactory.readValue(ctx.getMediaType(), ctx.getEntityStream(), cls);
                                }
                            }
                            if ("query".equals(in)) {
                                o = validator.convertAndValidate(uri.getQueryParameters().get(parameter.getName()), parameter, cls, definitions);
                            } else if ("path".equals(in)) {
                                o = validator.convertAndValidate(uri.getPathParameters().get(parameter.getName()), parameter, cls, definitions);
                            } else if ("header".equals(in)) {
                                o = validator.convertAndValidate(ctx.getHeaders().get(parameter.getName()), parameter, cls, definitions);
                            }
                        }
                        catch (ConversionException e) {
                            missingParams.add(e.getError());
                        }
                        catch (ValidationException e) {
                            missingParams.add(e.getValidationMessage());
                        }
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error("Couldn't find " + parameter.getName() + " (" + in + ") to " + parameterClasses[i], e);
                }

                args[i] = o;
                i += 1;
            }
            if(existingKeys.size() > 0) {
                LOGGER.error("extra keys: " + existingKeys);
//                Json.prettyPrint(this.operation);
            }
            if (missingParams.size() > 0) {
                StringBuilder builder = new StringBuilder();
                builder.append("Input error");
                if (missingParams.size() > 1) {
                    builder.append("s");
                }
                builder.append(": ");
                int count = 0;
                for (ValidationMessage message : missingParams) {
                    if (count > 0) {
                        builder.append(", ");
                    }
                    if(message != null && message.getMessage() != null) {
                        builder.append(message.getMessage());
                    }
                    else {
                        builder.append("no additional input");
                    }
                    count += 1;
                }
                int statusCode = config.getInvalidRequestStatusCode();
                ApiError error = new ApiError()
                  .code(statusCode)
                  .message(builder.toString());
                throw new ApiException(error);
            }
        }
        if(method != null) {
          LOGGER.info("calling method " + method + " on controller " + this.controller + " with args " + args);
          try {
              Object response = method.invoke(controller, args);
              if (response instanceof ResponseContext) {
                  ResponseContext wrapper = (ResponseContext) response;
                  ResponseBuilder builder = Response.status(wrapper.getStatus());
  
                  // response headers
                  for (String key : wrapper.getHeaders().keySet()) {
                      builder.header(key, wrapper.getHeaders().get(key));
                  }
  
                  // content type
                  if (wrapper.getContentType() != null) {
                      builder.type(wrapper.getContentType());
                  }
  
                  // entity
                  if (wrapper.getEntity() != null) {
                      builder.entity(wrapper.getEntity());
                  }
                  return builder.build();
              }
              return Response.ok().entity(response).build();
          } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
              LOGGER.error("failed to invoke method " + method, e);
              ApiError error = new ApiError()
                    .message("failed to invoke controller")
                    .code(500);
              throw new ApiException(error);
          }
        }
        Map<String, io.swagger.models.Response> responses = operation.getResponses();
        if (responses != null) {
            String[] keys = new String[responses.keySet().size()];
            Arrays.sort(responses.keySet().toArray(keys));
            int code = 0;
            String defaultKey = null;
            for (String key : keys) {
                if (key.startsWith("2")) {
                    defaultKey = key;
                    code = Integer.parseInt(key);
                    break;
                }
                if ("default".equals(key)) {
                    defaultKey = key;
                    code = 200;
                }
            }

            io.swagger.models.Response response = responses.get(defaultKey);
            Object output = ExampleBuilder.fromProperty(response.getSchema(), definitions);
            if (output != null) {
                return Response.status(code).entity(output).build();
            }
            return Response.status(code).build();
        }

        // TODO: might need to check possible response types
        return Response.ok().build();
    }
}
