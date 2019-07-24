/*
 *  Copyright 2017 SmartBear Software
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

package io.swagger.oas.test.integration;

import io.swagger.oas.inflector.Constants;
import io.swagger.oas.inflector.config.OpenAPIProcessor;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.oas.test.client.ApiClient;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import junit.framework.Assert;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class SwaggerListingIT {

    @Test
    public void verifySwaggerJson() throws Exception {
        OpenAPI openAPI = getJsonSwagger();
        assertNotNull(openAPI);
        assertEquals(openAPI.getInfo().getDescription(), "processed");
        verifySwaggerExtensions(openAPI);

    }

    @Test
    public void verifySwaggerYaml() throws Exception {
        OpenAPI openAPI = getYamlSwagger();
        assertNotNull(openAPI);
        assertEquals(openAPI.getInfo().getDescription(), "processed");
        verifySwaggerExtensions(openAPI);
    }

    private void verifySwaggerExtensions(OpenAPI openAPI) {
        openAPI.getPaths().forEach((k, p) -> {
            if (p.getPost() != null && p.getPost().getExtensions() != null) {
                assertNull(p.getPost().getExtensions().get("x-swagger-router-controller"));
            }
            if (p.getGet() != null && p.getGet().getExtensions() != null) {
                assertNull(p.getGet().getExtensions().get("x-swagger-router-controller"));
            }
        });
        openAPI.getComponents().getSchemas().forEach((k, s) -> {
            if (s.getProperties() != null) {
                for (Object o: s.getProperties().values()) {
                    Schema p = (Schema)o;
                    if (p.getExtensions() != null) {
                        assertNull(p.getExtensions().get("x-swagger-router-model"));
                    }
                }
            }
        });
    }

    @Test
    public void verifyVendorSpecExtensionsDelete() throws Exception {
        testVendorSpecExtensionDelete(getJsonSwagger());
        testVendorSpecExtensionDelete(getYamlSwagger());
    }

    private void testVendorSpecExtensionDelete(OpenAPI openAPI) throws Exception {
        for (String path : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(path);
            for (Operation operation : pathItem.readOperations()) {
                if (operation.getExtensions() != null) {
                    final Constants.VendorExtension filteredVendorExtension = getFilteredVendorExtensions(operation.getExtensions());
                    if (filteredVendorExtension != null) {
                        Assert.fail("Operation " + operation.getOperationId() + " contains " +
                                filteredVendorExtension.getValue());
                    }
                }
            }
        }
        Map<String,Schema> definition = openAPI.getComponents().getSchemas();
        for (String key : definition.keySet()) {
            if (definition.get(key).getExtensions() != null){
                final Constants.VendorExtension filteredVendorExtension =
                        getFilteredVendorExtensions(definition.get(key).getExtensions());
                if (filteredVendorExtension != null) {
                    Assert.fail("Model " + definition.get(key) + " contains " +
                            filteredVendorExtension.getValue());
                }
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

    private OpenAPI getJsonSwagger() throws Exception {
        ApiClient client = new ApiClient();

        String str = client.invokeAPI("swagger/openapi.json", "GET", new HashMap<String, String>(), null, new
                HashMap<String, String>(), null, "application/json", null, new String[0]);
        return Json.mapper().readValue(str, OpenAPI.class);
    }

    private OpenAPI getYamlSwagger() throws Exception {
        ApiClient client = new ApiClient();

        String str = client.invokeAPI("swagger/openapi.yaml", "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/yaml", null, new String[0]);
        LoggerFactory.getLogger(this.getClass().getName()).error("TEST string\n" + str);
        return Yaml.mapper().readValue(str, OpenAPI.class);
    }

    public static class SwaggerProcessorImpl implements OpenAPIProcessor {
        @Override
        public void process(OpenAPI openAPI) {

            openAPI.getInfo().setDescription("processed");
        }
    }
}
