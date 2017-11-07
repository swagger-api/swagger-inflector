package io.swagger.oas.inflector.validators;


import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.Iterator;

public interface Validator {
    void validate(Object argument, Parameter parameter, Iterator<Validator> next) throws ValidationException;
    void validate(Object argument, RequestBody body, Iterator<Validator> next) throws ValidationException;
}