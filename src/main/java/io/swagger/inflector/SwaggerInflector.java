package io.swagger.inflector;

import java.util.Map;

import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

public class SwaggerInflector extends ResourceConfig {
  public SwaggerInflector() {
    Swagger swagger = new SwaggerParser().read("swagger.yaml");

    Map<String, Path> paths = swagger.getPaths();
    Map<String, Model> definitions = swagger.getDefinitions();
    for(String pathString : paths.keySet()) {
      final Resource.Builder builder = Resource.builder();
      Path path = paths.get(pathString);
      builder.path(basePath(swagger.getBasePath(), pathString));
      Operation operation;

      operation = path.getGet();
      if(operation != null) {
        addOperation(builder, "GET", operation, definitions);
      }
      operation = path.getPost();
      if(operation != null) {
        addOperation(builder, "POST", operation, definitions);
      }
      operation = path.getPut();
      if(operation != null) {
        addOperation(builder, "PUT", operation, definitions);
      }
      operation = path.getDelete();
      if(operation != null) {
        addOperation(builder, "DELETE", operation, definitions);
      }
      operation = path.getOptions();
      if(operation != null) {
        addOperation(builder, "OPTIONS", operation, definitions);
      }
      operation = path.getPatch();
      if(operation != null) {
        addOperation(builder, "PATCH", operation, definitions);
      }
    }

    // add the swagger JSON
    enableSwaggerJSON(swagger);
    
    // add swagger YAML
    enableSwaggerYAML(swagger);

    // JSON
    register(JacksonJsonProvider.class);
    
    // Swagger serializers
    register(SwaggerSerializers.class);
  }
  
  private String basePath(String basePath, String path) {
    if(basePath == null || "".equals(basePath)) {
      return path;
    }
    return basePath + path;
  }
  
  private void enableSwaggerJSON(Swagger swagger) {
    final Resource.Builder builder = Resource.builder();
    builder.path(basePath(swagger.getBasePath(), "/swagger.json"))
      .addMethod("GET")
      .produces(MediaType.APPLICATION_JSON)
      .handledBy(new SwaggerResourceController(swagger))
      .build();

    registerResources(builder.build());      
  }

  private void enableSwaggerYAML(Swagger swagger) {
    final Resource.Builder builder = Resource.builder();
    builder.path(basePath(swagger.getBasePath(), "/swagger.yaml"))
      .addMethod("GET")
      .produces("application/yaml")
      .handledBy(new SwaggerResourceController(swagger))
      .build();

    registerResources(builder.build());

    // JSON
    register(JacksonJsonProvider.class);
    
    // Swagger serializers
    register(SwaggerSerializers.class);
  }

  private void addOperation(Resource.Builder builder, String method, Operation operation, Map<String, Model> definitions) {
    final ResourceMethod.Builder methodBuilder = builder.addMethod("GET");

    methodBuilder.handledBy(new OperationController(operation, definitions)).produces(MediaType.APPLICATION_JSON);

    final Resource resource = builder.build();
    registerResources(resource);      
  }
}
