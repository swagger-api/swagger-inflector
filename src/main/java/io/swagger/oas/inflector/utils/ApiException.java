package io.swagger.oas.inflector.utils;

import io.swagger.oas.inflector.models.ApiError;

public class ApiException extends RuntimeException {
  private static final long serialVersionUID = -753474495606091043L;

  private ApiError error;

  public ApiException() {
  }

  public ApiException(ApiError error) {
	super(error.getMessage());
	this.error = error;
  }

  public ApiException(ApiError error, Throwable cause) {
	super(error.getMessage(), cause);
	this.error = error;
  }
  
  public ApiError getError() {
    return error;
  }

  public void setError(ApiError error) {
    this.error = error;
  }
}
