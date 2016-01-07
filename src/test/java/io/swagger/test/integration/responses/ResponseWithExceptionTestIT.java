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

package io.swagger.test.integration.responses;

import io.swagger.test.client.ApiClient;
import io.swagger.test.client.ApiException;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import java.util.HashMap;

public class ResponseWithExceptionTestIT {
    private static final String MSG_TEMPLATE =
            "<ApiError><code>%d</code><message>%s</message></ApiError>";
    private final ApiClient client = new ApiClient();

    @Test
    public void verifyApiException() {
        try {
            client.invokeAPI("/throwApiException", "GET", new HashMap<String, String>(), null,
                    new HashMap<String, String>(), null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        } catch (ApiException e) {
            final Response.Status expected = Response.Status.CONFLICT;
            Assert.assertEquals(e.getCode(), expected.getStatusCode());
            Assert.assertEquals(e.getMessage(), String.format(MSG_TEMPLATE,
                    expected.getStatusCode(), expected.getReasonPhrase()));
        }
    }

    @Test
    public void verifyApiExceptionAsCause() {
        try {
            client.invokeAPI("/throwApiExceptionAsCause", "GET", new HashMap<String, String>(),
                    null, new HashMap<String, String>(), null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        } catch (ApiException e) {
            final Response.Status expected = Response.Status.CONFLICT;
            Assert.assertEquals(e.getCode(), expected.getStatusCode());
            Assert.assertEquals(e.getMessage(), String.format(MSG_TEMPLATE,
                    expected.getStatusCode(), expected.getReasonPhrase()));
        }
    }

    @Test
    public void verifyNonApiException() {
        try {
            client.invokeAPI("/throwNonApiException", "GET", new HashMap<String, String>(), null,
                    new HashMap<String, String>(), null, null, null, new String[0]);
            Assert.fail("Exception was expected!");
        } catch (ApiException e) {
            final Response.Status expected = Response.Status.INTERNAL_SERVER_ERROR;
            Assert.assertEquals(e.getCode(), expected.getStatusCode());
            Assert.assertEquals(e.getMessage(), String.format(MSG_TEMPLATE,
                    expected.getStatusCode(), "failed to invoke controller"));
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
