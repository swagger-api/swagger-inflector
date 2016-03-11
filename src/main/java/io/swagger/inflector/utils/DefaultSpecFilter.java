package io.swagger.inflector.utils;

import io.swagger.core.filter.AbstractSpecFilter;
import io.swagger.inflector.Constants;
import io.swagger.model.ApiDescription;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

import java.util.List;
import java.util.Map;

public class DefaultSpecFilter extends AbstractSpecFilter {

    @Override
    public boolean isOperationAllowed(Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(operation.getVendorExtensions() != null && operation.getVendorExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isParamAllowed(Parameter parameter, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(parameter.getVendorExtensions() != null && parameter.getVendorExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isPropertyAllowed(Model model, Property property, String propertyName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if(property.getVendorExtensions() != null && property.getVendorExtensions().containsKey(Constants.X_INFLECTOR_HIDDEN)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isRemovingUnreferencedDefinitions() {
        return true;
    }
}
