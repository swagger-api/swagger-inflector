package io.swagger.inflector.utils;


import io.swagger.inflector.models.ApiError;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
  public Response toResponse(Exception exception) {
    if(exception instanceof ApiException) {
      ApiException ae = (ApiException) exception;
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ae.getError()).build();
    }

    ApiError output = new ApiError()
      .code(500)
      .message(exception.getMessage());
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(output).build();
  }
}
