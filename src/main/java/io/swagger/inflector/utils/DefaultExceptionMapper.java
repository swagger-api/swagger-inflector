package io.swagger.inflector.utils;


import io.swagger.inflector.models.ApiError;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
  public Response toResponse(Exception exception) {
    if(exception instanceof ApiException) {
      ApiException ae = (ApiException) exception;
      return Response.status(ae.getError().getCode()).entity(ae.getError()).build();
    }
    else if (exception instanceof WebApplicationException) {
      WebApplicationException e = (WebApplicationException) exception;
      ApiError output = new ApiError()
        .code(e.getResponse().getStatus())
        .message(e.getMessage());
      return Response.status(e.getResponse().getStatus()).entity(output).build();
    }
    else {
      ApiError output = new ApiError()
        .code(500)
        .message(exception.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(output).build();
    }
  }
}
