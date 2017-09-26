package io.swagger.oas.inflector.utils;

import io.swagger.core.filter.AbstractSpecFilter;
import io.swagger.oas.inflector.Constants;
import io.swagger.model.ApiDescription;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import java.util.Optional;


import java.util.List;
import java.util.Map;

public class DefaultSpecFilter extends AbstractSpecFilter {

    @Override
    public Optional<Operation> filterOperation(Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(operation.getExtensions() != null && operation.getExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return Optional.of(operation);
        }
        return Optional.of(operation);
    }

    @Override
    public Optional<Parameter> filterParameter(Parameter parameter, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(parameter.getExtensions() != null && parameter.getExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return Optional.of(parameter);
        }
        return Optional.of(parameter);
    }

    @Override
    public Optional<Schema> filterSchemaProperty(Schema model, Schema property, String propertyName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(property.getExtensions() != null && property.getExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return  Optional.of(property);
        }
        return  Optional.of(property);
    }

    @Override
    public boolean isRemovingUnreferencedDefinitions() {
        return false;
    }
}
