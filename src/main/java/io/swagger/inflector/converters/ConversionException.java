package io.swagger.inflector.converters;

import io.swagger.inflector.validators.ValidationMessage;

public class ConversionException extends Exception {
  private static final long serialVersionUID = 1567596767061436973L;

  public ConversionException() {}

  public ConversionException(String message) {
    super(message);
  }

  public ValidationMessage getError() {
    // TODO Auto-generated method stub
    return null;
  }  
}