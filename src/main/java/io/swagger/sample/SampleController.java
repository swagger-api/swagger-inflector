package io.swagger.sample;

public class SampleController {
  public String testMe(Integer str) {
    return "{\"message\": \"got a " + str + "\"}";
  }
  
  public String withPath(String id) {
    return "{\"message\": \"got path " + id + "\"}";
  }
}
