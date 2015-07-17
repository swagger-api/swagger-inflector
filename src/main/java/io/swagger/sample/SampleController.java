package io.swagger.sample;

public class SampleController {
  public String testMe(Integer str) {
    return "got a " + str;
  }
  
  public String withPath(String id) {
    return "got path " + id;
  }
}
