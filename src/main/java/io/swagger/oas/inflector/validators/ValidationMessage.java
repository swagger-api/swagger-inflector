package io.swagger.oas.inflector.validators;

public class ValidationMessage {
    private ValidationError code;
    private String message;

    public ValidationMessage code(ValidationError code) {
        this.code = code;
        return this;
    }
    public ValidationMessage message(String message) {
        this.message = message;
        return this;
    }

    public ValidationError getCode() {
        return code;
    }
    public void setCode(ValidationError code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}