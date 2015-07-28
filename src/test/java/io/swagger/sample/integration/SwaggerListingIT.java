/*
 *  Copyright 2015 SmartBear Software
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

package io.swagger.sample.integration;

import io.swagger.models.Swagger;
import io.swagger.test.client.ApiClient;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.testng.Assert.assertNotNull;

public class SwaggerListingIT {
    @Test
    public void verifySwaggerJson() throws Exception {
        ApiClient client = new ApiClient();

        String str = client.invokeAPI("swagger.json", "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        Swagger swagger = Json.mapper().readValue(str, Swagger.class);

        assertNotNull(swagger);
    }

    @Test
    public void verifySwaggerYaml() throws Exception {
        ApiClient client = new ApiClient();

        String str = client.invokeAPI("swagger.yaml", "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/yaml", null, new String[0]);
        Swagger swagger = Yaml.mapper().readValue(str, Swagger.class);

        assertNotNull(swagger);
    }
}
