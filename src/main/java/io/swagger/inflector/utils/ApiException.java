package io.swagger.inflector.utils;

import io.swagger.inflector.models.ApiError;

public class ApiException extends RuntimeException {
  private static final long serialVersionUID = -753474495606091043L;

  private ApiError error;
  public ApiException(){}
  
  public ApiError getError() {
    return error;
  }

  public void setError(ApiError error) {
    this.error = error;
  }

  public ApiException(ApiError error) {
    this.error = error;
  }
}
