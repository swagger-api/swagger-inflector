package io.swagger.inflector.converters;

import io.swagger.inflector.validators.ValidationMessage;

public class ConversionException extends Exception {
  private static final long serialVersionUID = 1567596767061436973L;

  public ConversionException() {}

  private ValidationMessage message;

  public ConversionException message(ValidationMessage message) {
      this.message = message;
      return this;
  }
  
  public ValidationMessage getError() {
      return message;
  }
}