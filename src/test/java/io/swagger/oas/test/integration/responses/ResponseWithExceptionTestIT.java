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

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.swagger.oas.inflector.CustomMediaTypes;
import io.swagger.oas.inflector.models.ApiError;
import io.swagger.oas.test.client.ApiClient;
import io.swagger.oas.test.client.ApiException;
import io.swagger.util.Json;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResponseWithExceptionTestIT {
    private final ApiClient client = new ApiClient();

    @Test
    public void verifyApiExceptionAsJson() throws IOException {
        try {
            client.invokeAPI("/throwApiException", "GET", new HashMap<String, String>(), null,
                    new HashMap<String, String>(), null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        } catch (ApiException e) {
            final Response.Status expected = Response.Status.CONFLICT;
            Assert.assertEquals(e.getCode(), expected.getStatusCode());
            final ApiError error = Json.mapper().readValue(e.getMessage(), ApiError.class);
            Assert.assertEquals(error.getCode(), expected.getStatusCode());
            Assert.assertEquals(error.getMessage(), expected.getReasonPhrase());
        }
    }

    @Test
    public void verifyApiExceptionAsXml() throws IOException {
        try {
            final Map<String, String> headerParams = Collections.singletonMap(HttpHeaders.ACCEPT,
                    MediaType.APPLICATION_XML);
            client.invokeAPI("/throwApiException", "GET", new HashMap<String, String>(), null,
                    headerParams, null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        } catch (ApiException e) {
            final Response.Status expected = Response.Status.CONFLICT;
            Assert.assertEquals(e.getCode(), expected.getStatusCode());
            final ApiError error = new XmlMapper().readValue(e.getMessage(), ApiError.class);
            Assert.assertEquals(error.getCode(), expected.getStatusCode());
            Assert.assertEquals(error.getMessage(), expected.getReasonPhrase());
        }
    }

    @Test
    public void verifyApiExceptionForYaml() throws IOException {
        try {
            final Map<String, String> headerParams = Collections.singletonMap(HttpHeaders.ACCEPT,
                    CustomMediaTypes.APPLICATION_YAML.toString());
            client.invokeAPI("/throwApiException", "GET", new HashMap<String, String>(), null,
                    headerParams, null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        } catch (ApiException e) {
            final Response.Status expected = Response.Status.CONFLICT;
            Assert.assertEquals(e.getCode(), expected.getStatusCode());
            final ApiError error = Json.mapper().readValue(e.getMessage(), ApiError.class);
            Assert.assertEquals(error.getCode(), expected.getStatusCode());
            Assert.assertEquals(error.getMessage(), expected.getReasonPhrase());
        }
    }

    @Test
    public void verifyApiExceptionAsCause() throws IOException {
        try {
            client.invokeAPI("/throwApiExceptionAsCause", "GET", new HashMap<String, String>(),
                    null, new HashMap<String, String>(), null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        } catch (ApiException e) {
            final Response.Status expected = Response.Status.CONFLICT;
            Assert.assertEquals(e.getCode(), expected.getStatusCode());
            final ApiError error = Json.mapper().readValue(e.getMessage(), ApiError.class);
            Assert.assertEquals(error.getCode(), expected.getStatusCode());
            Assert.assertEquals(error.getMessage(), expected.getReasonPhrase());
        }
    }

    @Test
    public void verifyNonApiException() throws IOException {
        try {
            client.invokeAPI("/throwNonApiException", "GET", new HashMap<String, String>(), null,
                    new HashMap<String, String>(), null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        } catch (ApiException e) {
            final Response.Status expected = Response.Status.INTERNAL_SERVER_ERROR;
            Assert.assertEquals(e.getCode(), expected.getStatusCode());
            final ApiError error = Json.mapper().readValue(e.getMessage(), ApiError.class);
            Assert.assertEquals(error.getCode(), expected.getStatusCode());
            Assert.assertEquals(
                    error.getMessage().replaceFirst("\\(ID: [^\\)]+\\)$", "(ID: XXXXXXXX)"),
                    "There was an error processing your request. It has been logged (ID: XXXXXXXX)");
        }
    }

    @Test
    public void verifyNonRfc2616Status() {
        try {
            client.invokeAPI("/returnNonRfc2616Status", "GET", new HashMap<String, String>(), null,
                    new HashMap<String, String>(), null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        } catch (ApiException e) {
            Assert.assertEquals(e.getCode(), 422);
        }
    }
}
