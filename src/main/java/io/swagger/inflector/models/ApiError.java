package io.swagger.inflector.models;

public class ApiError {
  private int code;
  private String message;

  public ApiError code(int code) {
    this.code = code;
    return this;
  }
  public ApiError message(String message) {
    this.message = message;
    return this;
  }

  public int getCode() {
    return code;
  }
  public void setCode(int code) {
    this.code = code;
  }
  
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  } 
}
