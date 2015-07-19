package io.swagger.sample;

import io.swagger.util.Json;

import java.util.List;

import javax.ws.rs.core.Response;

public class SampleController {
  public Object postFormData(Long id, String name) {
    return "{\"id\": " + id + ", \"name\": \"" + name + "\"}";
  }

  public String goodbye(List<String> params) {
    return Json.pretty(params);
  }

  public String testMe(Integer str) {
    return "{\"message\": \"got a " + str + "\"}";
  }
  
  public String withPath(String id) {
    return "{\"message\": \"got path " + id + "\"}";
  }
}
