package io.swagger.oas.inflector.validators;

public class ValidationException extends Exception {
  private static final long serialVersionUID = 1785425151365385107L;
  private ValidationMessage message;

  public ValidationException message(ValidationMessage message) {
      this.message = message;
      return this;
  }

  public ValidationMessage getValidationMessage() {
      return message;
  } 
}