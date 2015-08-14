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
import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.inflector.validators.ValidationException;
import io.swagger.inflector.validators.ValidationMessage;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwaggerOperationController extends ReflectionUtils implements Inflector<ContainerRequestContext, Response> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerOperationController.class);

    private String path;
    private String httpMethod;
    private Operation operation;
    private Object controller = null;
    private Method method = null;
    private Class<?>[] parameterClasses = null;
    private Map<String, Model> definitions;
    private InputConverter validator;

    public SwaggerOperationController(Configuration config, String path, String httpMethod, Operation operation, Map<String, Model> definitions) {
        this.setConfiguration(config);
        this.path = path;
        this.httpMethod = httpMethod;
        this.operation = operation;
        this.definitions = definitions;

        this.validator = InputConverter.getInstance();

        Class<?>[] args = getOperationParameterClasses(operation, definitions);
        StringBuilder builder = new StringBuilder();

        builder.append(getMethodName(path, httpMethod, operation))
                .append("(");

        for (int i = 0; i < args.length; i++) {
            if (i == 0) {
                builder.append(RequestContext.class.getCanonicalName() + " request");
            } else {
                builder.append(", ");
                builder.append(args[i].getName());
                builder.append(" ").append(operation.getParameters().get(i - 1).getName());
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

                Class<?>[] args = getOperationParameterClasses(operation, this.definitions);
                Method[] methods = cls.getMethods();
                for (Method method : methods) {
                    if (methodName.equals(method.getName())) {
                        Class<?>[] methodArgs = method.getParameterTypes();
                        if (methodArgs.length == args.length) {
                            int i = 0;
                            boolean matched = true;
                            if (!args[i].equals(methodArgs[i])) {
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
                            if (formDataString == null) {
                                // can only read stream once
                                formDataString = IOUtils.toString(ctx.getEntityStream(), "UTF-8");
                            }
                            if (formDataString != null) {
                                // TODO need to decode
                                String[] parts = formDataString.split("&");
                                for (String part : parts) {
                                    String[] kv = part.split("=");
                                    if (kv != null && kv.length == 2) {
                                        // TODO how to handle arrays here?
                                        String key = kv[0];
                                        String value = kv[1];
                                        if (parameter.getName().equals(key)) {
                                            try {
                                                o = validator.convertAndValidate(Arrays.asList(value), parameter, parameterClasses[i+1], definitions);
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
                    else {
                        try {
                            if ("body".equals(in)) {
                                if (ctx.hasEntity()) {
                                    o = EntityProcessorFactory.readValue(ctx.getMediaType(), ctx.getEntityStream(), parameterClasses[i]);
                                }
                            }
                            if ("query".equals(in)) {
                                o = validator.convertAndValidate(uri.getQueryParameters().get(parameter.getName()), parameter, parameterClasses[i], definitions);
                            } else if ("path".equals(in)) {
                                o = validator.convertAndValidate(uri.getPathParameters().get(parameter.getName()), parameter, parameterClasses[i], definitions);
                            } else if ("header".equals(in)) {
                                o = validator.convertAndValidate(ctx.getHeaders().get(parameter.getName()), parameter, parameterClasses[i], definitions);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }

                args[i] = o;
                i += 1;
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
                    builder.append(message.getMessage());
                    count += 1;
                }
                int statusCode = config.getInvalidRequestStatusCode();
                return Response.status(statusCode)
                    .entity(new ApiError()
                        .code(statusCode)
                        .message(builder.toString())).build();
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
