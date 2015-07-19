package io.swagger.inflector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.swagger.inflector.config.*;
import io.swagger.inflector.models.ApiError;
import io.swagger.inflector.utils.*;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import io.swagger.util.Json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.process.Inflector;

public class OperationController extends ReflectionUtils implements Inflector<ContainerRequestContext, Response> {
  private static final Logger LOGGER = LoggerFactory.getLogger(OperationController.class);

  private String path;
  private String httpMethod;
  private Operation operation;
  private Object controller = null;
  private Method method = null;
  private Class<?>[] parameterClasses = null;
  private Map<String, Model> definitions;

  public OperationController(String path, String httpMethod, Operation operation, Map<String, Model> definitions) {
    this.setConfiguration(new Configuration());
    this.path = path;
    this.httpMethod = httpMethod;
    this.operation = operation;
    this.definitions = definitions;

    Class<?>[] args = getOperationParameterClasses(operation, definitions);
    StringBuilder builder = new StringBuilder();

    builder.append(getMethodName(path, httpMethod, operation))
      .append("(");
    for(int i = 0; i < args.length; i++) {
      if(i > 0)
        builder.append(", ");
      builder.append(args[i].getName());
      builder.append(" ").append(operation.getParameters().get(i).getName());
    }
    builder.append(")");

    LOGGER.debug("looking for operation: " + builder.toString());

    this.method = detectMethod(operation);
    if(method == null) {
      LOGGER.debug("no method to map to, using mock response");
    }
    else {
      LOGGER.debug("found method!");
    }
  }
  
  public Method detectMethod(Operation operation) {
    String methodName = getMethodName(path, httpMethod, operation);
    String controller = getControllerName(operation);

    if(controller != null && methodName != null) {
      // find the controller!
      try {
        Class<?> cls = Class.forName(controller);
        if(cls != null) {
          Class<?>[] args = getOperationParameterClasses(operation, this.definitions);
          Method [] methods = cls.getMethods();
          for(Method method : methods) {
            if(methodName.equals(method.getName())) {
              Class<?>[] methodArgs = method.getParameterTypes();
              if(methodArgs.length == args.length) {
                int i = 0;
                boolean matched = true;
                if(!args[i].equals(methodArgs[i])) {
                  LOGGER.info("failed to match " + args[i] + ", " + methodArgs[i]);
                  matched = false;
                }
                if(matched) {
                  this.parameterClasses = args;
                  this.controller = cls.newInstance();
                  return method;
                }
              }
            }
          }
        }
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
  
  // not sure if this is needed
  public String getMethodSignature() {
    if(method != null && parameterClasses != null) {
      StringBuilder builder = new StringBuilder();
      builder.append("public void ")
        .append(method.getName())
        .append("(");
      for(int i = 0; i < parameterClasses.length; i++) {
        if(i > 0)
          builder.append(", ");
        builder.append(parameterClasses[i].getName());
        builder.append(" ").append(operation.getParameters().get(i).getName());
      }
      builder.append(")");
      
      return builder.toString();
    }
    else return "unknown!";
  }

  @Override
  public Response apply(ContainerRequestContext ctx) {
    ctx.getMethod();
    List<Parameter> parameters = operation.getParameters();
    Object[] args = new Object[parameters.size()];
    if(method != null && parameters != null) {
      int i = 0;

      List<Parameter> missingParams = new ArrayList<Parameter>();
      UriInfo uri = ctx.getUriInfo();
      
      for(Parameter parameter : parameters) {
        String in = parameter.getIn();
        Object o = null;
        
        try {
          if("query".equals(in)) {
            o = cast(uri.getQueryParameters().get(parameter.getName()), parameter, parameterClasses[i]);
          }
          else if("path".equals(in)) {
            o = cast(uri.getPathParameters().get(parameter.getName()), parameter, parameterClasses[i]);
          }
          else if("header".equals(in)) {
            o = cast(ctx.getHeaders().get(parameter.getName()), parameter, parameterClasses[i]);
          }
          else if("formData".equals(in)) {
            throw new RuntimeException("not implemented yet!");
          }
          else if("body".equals(in)) {
            if(ctx.hasEntity()) {
              try {
                // TODO JSON only!
                o = Json.mapper().readValue(ctx.getEntityStream(), parameterClasses[i]);
              } catch (JsonParseException e) {
                e.printStackTrace();
              } catch (JsonMappingException e) {
                e.printStackTrace();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        }
        catch (NumberFormatException e) {
          System.out.println("oops! Couldn't find " + parameter.getName() + " (" + in + ") to " + parameterClasses[i]);
        }
        if(o == null && parameter.getRequired()) {
          missingParams.add(parameter);
        }
          
        args[i] = o;
        i += 1;
      }
      
      if(missingParams.size() > 0) {
        StringBuilder builder = new StringBuilder();
        builder.append("Missing required Parameter");
        if(missingParams.size() > 1)
          builder.append("s");
        builder.append(" ");
        int count = 0;
        for(Parameter parameter : missingParams) {
          if(count > 0)
            builder.append(", ");
          builder.append(parameter.getName() + " (" + parameter.getIn() + ")");
          count += 1;
        }
        return Response.status(Status.BAD_REQUEST).entity(new ApiError(Status.BAD_REQUEST.getStatusCode(), builder.toString())).build();
      }
      LOGGER.info("calling method " + method + " on controller " + this.controller + " with args " + args);
      try {
        return Response.ok().entity(method.invoke(controller, args)).build();
      } catch (IllegalArgumentException | IllegalAccessException
          | InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    Map<String, io.swagger.models.Response> responses = operation.getResponses();
    if(responses != null) {
      for(String code : responses.keySet()) {
        io.swagger.models.Response response = responses.get(code);
        return Response.status(Integer.parseInt(code)).entity(ExampleBuilder.fromProperty(response.getSchema(), definitions)).build();
      }
    }
    return Response.ok().entity("fun!").build();
  }

  public Object cast(List<String> o, Parameter parameter, Class<?> cls) {
    if(o == null || o.size() == 0)
      return null;

    LOGGER.debug("casting `" + o + "` to " + cls);
    if(List.class.equals(cls)) {
      if(parameter instanceof SerializableParameter) {
        List<Object> output = new ArrayList<Object>();
        SerializableParameter sp = (SerializableParameter) parameter;
        if(sp.getItems() != null) {
          Property inner = sp.getItems();

          // TODO: this does not need to be done this way, update the helper method
          Parameter innerParam = new QueryParameter().property(inner);
          Class<?> innerClass = getParameterSignature(innerParam, definitions);
          for(String obj : o) {
            String[] parts = new String[0];
            // TODO: make much smarter
            if("csv".equals(sp.getCollectionFormat()) && !StringUtils.isEmpty(obj)) {
              parts = obj.split(",");
            }
            if("pipes".equals(sp.getCollectionFormat()) && !StringUtils.isEmpty(obj)) {
              parts = obj.split("|");
            }
            if("ssv".equals(sp.getCollectionFormat()) && !StringUtils.isEmpty(obj)) {
              parts = obj.split(" ");
            }
            for(String p : parts) {
              Object ob = cast(p, inner, innerClass);
              if(ob != null) {
                output.add(ob);
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

}
