package io.swagger.inflector;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.process.Inflector;


public class SampleController implements Inflector<ContainerRequestContext, Response> {

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    // TODO Auto-generated method stub
    containerRequestContext.getHeaders();
    containerRequestContext.getCookies();
    containerRequestContext.hasEntity();

    containerRequestContext.getUriInfo().getPath();
    containerRequestContext.getUriInfo().getPathParameters();
    containerRequestContext.getUriInfo().getQueryParameters();
    
    return Response.ok().entity("got it!").build();
  }
}
