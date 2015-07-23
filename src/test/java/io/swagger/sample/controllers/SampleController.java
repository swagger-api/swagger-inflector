package io.swagger.sample.controllers;

import io.swagger.inflector.models.ApiError;
import io.swagger.sample.models.User;
import io.swagger.util.Json;

import java.util.List;

import javax.ws.rs.core.Response;

public class SampleController {
  public Object postFormData(Long id, String name) {
    // just showing a sample response
    return new User()
      .id(id)
      .user(name);
  }
}
