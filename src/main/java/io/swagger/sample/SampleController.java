package io.swagger.sample;

import io.swagger.util.Json;

import java.util.List;

public class SampleController {
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
