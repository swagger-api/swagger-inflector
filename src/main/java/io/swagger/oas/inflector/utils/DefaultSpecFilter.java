package io.swagger.oas.inflector.utils;

import io.swagger.core.filter.AbstractSpecFilter;
import io.swagger.oas.inflector.Constants;
import io.swagger.model.ApiDescription;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;


import java.util.List;
import java.util.Map;

public class DefaultSpecFilter extends AbstractSpecFilter {

    @Override
    public boolean isOperationAllowed(Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(operation.getExtensions() != null && operation.getExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isParamAllowed(Parameter parameter, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(parameter.getExtensions() != null && parameter.getExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isPropertyAllowed(Schema model, Schema property, String propertyName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(property.getExtensions() != null && property.getExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isRemovingUnreferencedDefinitions() {
        return false;
    }
}
