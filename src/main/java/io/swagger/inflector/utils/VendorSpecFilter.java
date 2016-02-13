/*
 *  Copyright 2016 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.swagger.inflector.utils;

import io.swagger.core.filter.SpecFilter;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.inflector.Constants;
import io.swagger.model.ApiDescription;
import io.swagger.models.Model;
import io.swagger.models.Operation;

import java.util.List;
import java.util.Map;

public class VendorSpecFilter extends SpecFilter {

    @Override
    public Map<String, Model> filterDefinitions(SwaggerSpecFilter filter, Map<String, Model> definitions,
            Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        final Map<String, Model> filteredDefinitions = super.filterDefinitions(filter, definitions, params, cookies,
                headers);

        for (Model model : filteredDefinitions.values()) {
            filterVendorExtensions(model.getVendorExtensions());
        }

        return filteredDefinitions;
    }

    @Override
    public Operation filterOperation(SwaggerSpecFilter filter, Operation op, ApiDescription api,
            Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        final Operation operation = super.filterOperation(filter, op, api, params, cookies, headers);

        filterVendorExtensions(operation.getVendorExtensions());

        return operation;
    }

    private void filterVendorExtensions(Map<String, Object> vendorExtensions) {
        for (Constants.VendorExtension vendorExtension : Constants.VendorExtension.values()) {
            vendorExtensions.remove(vendorExtension.getValue());
        }
    }
}
