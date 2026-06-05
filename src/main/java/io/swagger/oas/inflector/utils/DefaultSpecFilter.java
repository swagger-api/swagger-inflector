package io.swagger.oas.inflector.utils;

import io.swagger.oas.inflector.Constants;
import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;


import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultSpecFilter extends AbstractSpecFilter {

    @Override
    public Optional<Operation> filterOperation(Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(operation.getExtensions() != null && operation.getExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return Optional.empty();
        }
        return Optional.of(operation);

    }

    @Override
    public Optional<Parameter> filterParameter(Parameter parameter, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(parameter.getExtensions() != null && parameter.getExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return Optional.empty();
        }
        return Optional.of(parameter);

    }

    @Override
    public Optional<Schema> filterSchemaProperty(Schema property, Schema model, String propertyName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(property.getExtensions() != null && property.getExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return Optional.empty();
        }
        return Optional.of(property);
    }

    @Override
    public boolean isRemovingUnreferencedDefinitions() {
        return false;
    }
}
