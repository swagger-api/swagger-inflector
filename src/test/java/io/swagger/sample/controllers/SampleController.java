package io.swagger.sample.controllers;

import io.swagger.inflector.models.ApiError;
import io.swagger.sample.models.User;
import io.swagger.util.Json;

import java.util.List;

import javax.ws.rs.core.Response;

public class SampleController {
  public Object postFormData(Long id, String name) {
    return new User()
      .id(id)
      .user(name);
  }

  public Object goodbye(List<String> params) {
    return Json.pretty(params);
  }

  public Object testMe(Integer id) {
    return new ApiError()
      .code(id)
      .message("ok!");
  }
  
  public Object withPath(String id) {
    return new ApiError()
      .code(200)
      .message(id);
  }
}
