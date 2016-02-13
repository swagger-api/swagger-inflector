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

package io.swagger.test.integration;

import io.swagger.inflector.Constants;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.test.client.ApiClient;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertNotNull;

public class SwaggerListingIT {

    @Test
    public void verifySwaggerJson() throws Exception {
        Swagger swagger = getJsonSwagger();
        assertNotNull(swagger);
    }

    @Test
    public void verifySwaggerYaml() throws Exception {
        Swagger swagger = getYamlSwagger();
        assertNotNull(swagger);
    }

    @Test
    public void verifyVendorSpecExtensionsDelete() throws Exception {
        testVendorSpecExtensionDelete(getJsonSwagger());
        testVendorSpecExtensionDelete(getYamlSwagger());
    }

    private void testVendorSpecExtensionDelete(Swagger swagger) throws Exception {
        for (Path path : swagger.getPaths().values()) {
            for (Operation operation : path.getOperations()) {
                final Constants.VendorExtension filteredVendorExtension =
                        getFilteredVendorExtensions(operation.getVendorExtensions());
                if (filteredVendorExtension != null) {
                    Assert.fail("Operation " + operation.getOperationId() + " contains " +
                            filteredVendorExtension.getValue());
                }
            }
        }
        for (Map.Entry<String, Model> definition : swagger.getDefinitions().entrySet()) {
            final Constants.VendorExtension filteredVendorExtension =
                    getFilteredVendorExtensions(definition.getValue().getVendorExtensions());
            if (filteredVendorExtension != null) {
                Assert.fail("Model " + definition.getKey() + " contains " +
                        filteredVendorExtension.getValue());
            }
        }
    }

    private Constants.VendorExtension getFilteredVendorExtensions(Map<String, Object> vendorExtensions) {
        final Set<String> extensionNames = vendorExtensions.keySet();
        for (Constants.VendorExtension vendorExtension : Constants.VendorExtension.values()) {
            if (extensionNames.contains(vendorExtension.getValue())) {
                return vendorExtension;
            }
        }
        return null;
    }

    private Swagger getJsonSwagger() throws Exception {
        ApiClient client = new ApiClient();

        String str = client.invokeAPI("swagger.json", "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        return Json.mapper().readValue(str, Swagger.class);
    }

    private Swagger getYamlSwagger() throws Exception {
        ApiClient client = new ApiClient();

        String str = client.invokeAPI("swagger.yaml", "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/yaml", null, new String[0]);
        return Yaml.mapper().readValue(str, Swagger.class);
    }
}
