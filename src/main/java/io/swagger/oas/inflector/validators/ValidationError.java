package io.swagger.oas.inflector.validators;

public enum ValidationError {
  MISSING_REQUIRED,
  VALUE_UNDER_MINIMUM,
  VALUE_OVER_MAXIMUM,
  INVALID_FORMAT,
  UNACCEPTABLE_VALUE
}