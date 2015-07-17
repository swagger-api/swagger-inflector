package io.swagger.inflector;

import io.swagger.models.Swagger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.process.Inflector;

public class SwaggerResourceController implements Inflector<ContainerRequestContext, Response> {
  private Swagger swagger;

  public SwaggerResourceController(Swagger swagger) {
    this.swagger = swagger;
  }

  @Override
  public Response apply(ContainerRequestContext arg0) {
    return Response.ok().entity(swagger).build();
  }
}
