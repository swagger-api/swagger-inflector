package io.swagger.sample.controllers;

import io.swagger.inflector.models.ApiError;
import io.swagger.sample.models.User;
import io.swagger.util.Json;

import java.util.List;

import javax.ws.rs.core.Response;

public class SampleController {
  public Object addPet(io.swagger.sample.models.Pet body) {
    return new User()
      .id(123L)
      .user("fehguy");
  }
}
