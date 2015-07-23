package io.swagger.sample.controllers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import io.swagger.inflector.models.RequestWrapper;
import io.swagger.inflector.models.ResponseWrapper;
import io.swagger.sample.models.User;

public class TestController {
  public ResponseWrapper postFormData(RequestWrapper request, Long id, String name) {
    // just showing a sample response

    return new ResponseWrapper()
      .status(Status.OK)
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .entity(new User()
        .id(id)
        .user(name));
  }
}
