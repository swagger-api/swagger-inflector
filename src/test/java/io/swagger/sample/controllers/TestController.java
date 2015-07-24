package io.swagger.sample.controllers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import io.swagger.sample.models.User;

public class TestController {
  public ResponseContext postFormData(RequestContext request, Long id, String name) {
    // just showing a sample response

    return new ResponseContext()
      .status(Status.OK)
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .entity(new User()
        .id(id)
        .user(name));
  }
}
