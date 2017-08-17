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

package io.swagger.oas.test.integration.responses;

import io.swagger.oas.test.client.ApiClient;
import io.swagger.oas.test.client.ApiException;
import io.swagger.oas.test.models.Address;
import io.swagger.util.Json;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SchemaResponseIT {
    private final ApiClient client = new ApiClient();

    @Test
    public void testValidResponse() throws Exception {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("street", "12345");
        final String response = client.invokeAPI("/withModel/3", "POST",
                new HashMap<String, String>(), body, new HashMap<String, String>(), null, null,
                null, new String[0]);
        final Address asObject = Json.mapper().readValue(response, Address.class);
        Assert.assertEquals(asObject.getStreet(), "3 street");
    }

    @Test
    public void testInvalidRequestPayload() throws Exception {
        try {
            client.invokeAPI("/withModel/-1", "POST", new HashMap<String, String>(), "blah blah",
                    new HashMap<String, String>(), null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        }
        catch (ApiException e) {
            assertEquals(e.getCode(), 400);
        }
    }

    @Test
    public void testInvalidResponsePayload() throws Exception {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("street", "12345");
        try {
            client.invokeAPI("/withModel/-1", "POST", new HashMap<String, String>(), body,
                    new HashMap<String, String>(), null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        }
        catch (ApiException e) {
            assertEquals(e.getCode(), 400);
        }
    }

    @Test
    public void testIssue130() throws Exception {
        Map<String, Object> body = new HashMap<String, Object>();
        String response = client.invokeAPI("/issue-130", "GET", new HashMap<String, String>(), body,
                new HashMap<String, String>(), null, null, "application/json", new String[0]);

        assertEquals(response, "{\"aStringArray\":[\"a string value\"],\"anIntegerArray\":[99]}");
    }

    @Test
    public void testResponseHeaders() throws Exception {
        Map<String, Object> body = new HashMap<String, Object>();
        try {
            client.invokeAPI("/issue-132.2", "GET", new HashMap<String, String>(), body,
                    new HashMap<String, String>(), null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        }
        catch (ApiException e) {
            assertEquals(e.getCode(), 303);
            assertTrue(e.getResponseHeaders() != null);
            List<String> value = e.getResponseHeaders().get("_links");
            assertEquals(value.get(0).toString(), "[http://foo.bar/other]");
        }
    }

    /**
     * ensure the response honors the produces type with no explicit Accept header
     * @throws Exception
     */
    @Test
    public void testProducesType() throws Exception {
        String response = client.invokeAPI("/producesTest", "GET", new HashMap<String, String>(), new HashMap<String, Object>(),
                new HashMap<String, String>(), null, null, null, new String[0]);

        assertEquals(response, "{\"name\":\"string\"}");
    }

    /**
     * ensure the response honors the produces type with incompatible Accept header
     * @throws Exception
     */
    @Test
    public void testProducesTypeWithConflict() throws Exception {
        String response = client.invokeAPI("/producesTest", "GET", new HashMap<String, String>(), new HashMap<String, Object>(),
                new HashMap<String, String>(), null, null, "application/xml", new String[0]);

        assertEquals("{\"name\":\"string\"}", response);
    }
}
