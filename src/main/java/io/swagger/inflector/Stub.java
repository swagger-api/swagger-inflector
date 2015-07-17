package io.swagger.inflector;

import java.util.List;
import java.util.Map;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


public class Stub extends ResourceConfig {
  public Stub() {
    Operation testMe = new Operation()
      .operationId("testMe")
      .parameter(new QueryParameter()
        .name("limit")
        .required(true)
        .property(new IntegerProperty()));

    testMe.setVendorExtension("x-swagger-router-controller", "io.swagger.sample.SampleController");
    
    Operation withPath = new Operation()
      .operationId("withPath")
      .parameter(new PathParameter()
        .name("id")
        .property(new StringProperty()));

    withPath.setVendorExtension("x-swagger-router-controller", "io.swagger.sample.SampleController");
    
    Swagger swagger = new Swagger()
      .path("/hello", new Path()
          .get(testMe))
      .path("/{id}", new Path()
          .get(withPath))
      .path("/goodbye", new Path()
          .get(new Operation()
              .parameter(new QueryParameter()
                  .name("happy")
                  .required(true)
                  .property(new BooleanProperty()))));

    
    Map<String, Path> paths = swagger.getPaths();
    for(String pathString : paths.keySet()) {
      final Resource.Builder builder = Resource.builder();
      Path path = paths.get(pathString);
      builder.path(pathString);
      Operation operation;
      
      operation = path.getGet();
      if(operation != null) {
        addOperation(builder, "GET", operation);
      }
      operation = path.getPost();
      if(operation != null) {
        addOperation(builder, "POST", operation);
      }
      operation = path.getPut();
      if(operation != null) {
        addOperation(builder, "PUT", operation);
      }
      operation = path.getDelete();
      if(operation != null) {
        addOperation(builder, "DELETE", operation);
      }
      operation = path.getOptions();
      if(operation != null) {
        addOperation(builder, "OPTIONS", operation);
      }
      operation = path.getPatch();
      if(operation != null) {
        addOperation(builder, "PATCH", operation);
      }
    }
    register(JacksonJsonProvider.class);
  }
  
  private void addOperation(Resource.Builder builder, String method, Operation operation) {
    final ResourceMethod.Builder methodBuilder = builder.addMethod("GET");

    methodBuilder.handledBy(new SwaggerController(operation)).produces(MediaType.APPLICATION_JSON);

    final Resource resource = builder.build();
    registerResources(resource);      
  }
}
