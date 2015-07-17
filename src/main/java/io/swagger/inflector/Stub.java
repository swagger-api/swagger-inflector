package io.swagger.inflector;

import java.util.Map;

import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


public class Stub extends ResourceConfig {
  public Stub() {
    Operation testMe = new Operation()
      .tag("test1")
      .operationId("testMe")
      .parameter(new QueryParameter()
        .name("limit")
        .required(true)
        .property(new IntegerProperty()))
       .response(200, new Response()
           .description("fun!")
           .schema(new StringProperty()));
    testMe.setVendorExtension("x-swagger-router-controller", "io.swagger.sample.SampleController");
    
    Operation withPath = new Operation()
      .tag("test1")
      .operationId("withPath")
      .parameter(new PathParameter()
        .name("id")
        .property(new StringProperty()))
      .response(200, new Response()
        .description("success!")
        .schema(new RefProperty("User")));
    withPath.setVendorExtension("x-swagger-router-controller", "io.swagger.sample.SampleController");
    
    Operation withModel = new Operation()
      .tag("test2")
      .operationId("withModel")
      .parameter(new PathParameter()
        .name("id")
        .property(new StringProperty()))
      .response(200, new Response()
        .description("success!")
        .schema(new RefProperty("Animal")));
    
    Swagger swagger = new Swagger()
      .path("/hello", new Path()
        .get(testMe))
      .path("/withPath/{id}", new Path()
        .get(withPath))
      .path("/withModel/{id}", new Path()
        .get(withModel))
      .path("/goodbye", new Path()
        .get(new Operation()
          .tag("test2")
          .response(200, new Response()
            .description("success!")
            .schema(new RefProperty("User")))
          .parameter(new QueryParameter()
            .name("happy")
            .required(true)
            .property(new BooleanProperty()))))
       .model("User", new ModelImpl()
         .example("{\"foo\":\"bar\"}")
         .property("id", new LongProperty())
         .property("name", new StringProperty())
           .description("the name!"))
       .model("Address",  new ModelImpl()
         .property("street", new StringProperty().example("12345 El Monte Road"))
         .property("city", new StringProperty().example("Los Altos Hills"))
         .property("state", new StringProperty().example("CA"))
         .property("zip", new StringProperty().example("94022")))
       .model("Animal", new ModelImpl()
         .property("id", new LongProperty())
         .property("name", new StringProperty())
           .description("the name!")
         .property("address", new RefProperty("Address")));
    
    Map<String, Path> paths = swagger.getPaths();
    Map<String, Model> definitions = swagger.getDefinitions();
    for(String pathString : paths.keySet()) {
      final Resource.Builder builder = Resource.builder();
      Path path = paths.get(pathString);
      builder.path(pathString);
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
    // add the swagger response
    final Resource.Builder builder = Resource.builder();
    builder.path("/swagger.json")
      .addMethod("GET")
      .produces(MediaType.APPLICATION_JSON, "application/yaml")
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

    methodBuilder.handledBy(new SwaggerController(operation, definitions)).produces(MediaType.APPLICATION_JSON);

    final Resource resource = builder.build();
    registerResources(resource);      
  }
}
