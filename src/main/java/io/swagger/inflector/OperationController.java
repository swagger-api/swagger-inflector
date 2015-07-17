package io.swagger.inflector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.swagger.inflector.models.ApiError;
import io.swagger.inflector.utils.ExampleBuilder;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Null;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.process.Inflector;

public class OperationController implements Inflector<ContainerRequestContext, Response> {
  private static final Logger LOGGER = LoggerFactory.getLogger(OperationController.class);

  private Operation operation;
  private Object controller = null;
  private Method method = null;
  private Class<?>[] parameterClasses = null;
  Map<String, Model> definitions;

  public OperationController(Operation operation, Map<String, Model> definitions) {
    this.operation = operation;
    this.definitions = definitions;

    this.method = detectMethod(operation);
    if(method == null) {
      LOGGER.debug("no method to map to, using mock response");
    }
    else {
      LOGGER.debug("found method! Signature is:");
      LOGGER.debug(getMethodSignature());
    }
  }
  
  public Method detectMethod(Operation operation) {
    String methodName = operation.getOperationId();
    String controller = (String)operation.getVendorExtensions().get("x-swagger-router-controller");
    if(controller != null && methodName != null) {
      // find the controller!
      try {
        Class<?> cls = Class.forName(controller);
        if(cls != null) {
          Class<?>[] args = getOperationSignature(operation);
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
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
  
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
  
  public Class<?>[] getOperationSignature(Operation operation) {
    Class<?>[] classes = new Class<?>[operation.getParameters().size()];
    int i = 0;
    for(Parameter parameter : operation.getParameters()) {
      Class<?> argumentClass = getParameterSignature(parameter);
      classes[i] = argumentClass;
      i += 1;
    }
    return classes;
  }
  
  public Class<?> getParameterSignature(Parameter parameter) {
    if(parameter instanceof SerializableParameter) {
      SerializableParameter sp = (SerializableParameter) parameter;
      String type = sp.getType();
      String format = sp.getFormat();
      switch (type) {
      case "string":
        return String.class;
      case "integer":
        if("int32".equals(format)) {
          return Integer.class;
        }
        if("int64".equals(format)) {
          return Long.class;
        }
        break;
      case "number":
        if("double".equals(format)) {
          return Double.class;
        }
        if("float".equals(format)) {
          return Float.class;
        }
        return BigDecimal.class;
      case "boolean":
        return Boolean.class;
      }
      LOGGER.error("oops! Couldn't match " + type + ", " + format);
    }
    else {
      LOGGER.error("oops! Not implemented");
    }
    return Null.class;
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
            throw new RuntimeException("not implemented yet!");
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

    if(Integer.class.equals(cls)) {
      return Integer.parseInt(o.get(0));
    }
    if(Long.class.equals(cls)) {
      return Long.parseLong(o.get(0));
    }
    if(Float.class.equals(cls)) {
      return Float.parseFloat(o.get(0));
    }
    if(Double.class.equals(cls)) {
      return Double.parseDouble(o.get(0));
    }
    if(String.class.equals(cls)) {
      return o.get(0);
    }
    if(Boolean.class.equals(cls)) {
      return Boolean.parseBoolean(o.get(0));
    }
    return null;
  }
}
