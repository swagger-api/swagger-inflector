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

import io.swagger.oas.test.client.ApiClient;
import io.swagger.oas.test.client.ApiException;
import io.swagger.oas.test.models.Address;
import io.swagger.oas.test.models.ExtendedAddress;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

public class RequestTestIT {
    ApiClient client = new ApiClient();

    @Test
    public void verifyValidDateTimeInput() throws Exception {
        String path = "/withDate/" + new DateTime().toString();
        String str = client.invokeAPI(path, "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertNotNull(str);
    }

    @Test(expectedExceptions = ApiException.class)
    public void verifyInvalidDateTimeInput() throws Exception {
        String path = "/withDate/booyah";

        String str = client.invokeAPI(path, "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertNotNull(str);
    }

    @Test
    public void verifyModelMappingFromExtensions() throws Exception {
        String path = "/withModel/3";
        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "{\"street\":\"3 street\"}");
    }

    @Test
    public void verifyArrayModelMapping() throws Exception {
        final Address first = new Address();
        first.setStreet("first");
        final Address second = new Address();
        second.setStreet("second");
        client.invokeAPI("/withModelArray/3", "POST", new HashMap<String, String>(),
                Arrays.asList(first, second), new HashMap<String, String>(), null,
                "application/json", null, new String[0]);
    }

    @Test
    public void verifyComposedModelValidation() throws Exception {
        try {
            client.invokeAPI("/withInvalidComposedModel", "POST", new HashMap<String, String>(),
                    new ExtendedAddress(), new HashMap<String, String>(), null, "application/json",
                    null, new String[0]);
            Assert.fail("No exception was thrown");
        } catch (ApiException e) {
            Assert.assertEquals(e.getCode(), HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    @Test()
    public void verifyComposedModelArrayValidation() throws Exception {
        try {
            client.invokeAPI("/withInvalidComposedModelArray", "POST", new HashMap<String, String>(),

                    Arrays.asList(new ExtendedAddress(), new ExtendedAddress()),
                    new HashMap<String, String>(), null, "application/json", null, new String[0]);
            Assert.fail("No exception was thrown");
        } catch (ApiException e) {
            Assert.assertEquals(e.getCode(), HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    @Test
    public void verifyComposedModelDeserialization() throws Exception {
        final ExtendedAddress body = new ExtendedAddress();
        body.setGps("gps");
        body.setStreet("street");
        client.invokeAPI("/withValidComposedModel", "POST", new HashMap<String, String>(),
                body, new HashMap<String, String>(), null, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, new String[0]);
    }

    @Test
    public void verifyStringArrayQueryParam() throws Exception {
        client.setDebugging(true);

        String path = "/arrayInputTest";
        Map<String, String> queryParameters = new HashMap<String, String>();

        queryParameters.put("users", "a,b,c");
        String str = client.invokeAPI(path, "GET", queryParameters, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "[\"a\",\"b\",\"c\"]");
    }

    @Test
    public void verifyStringPostBody() throws Exception {
        client.setDebugging(true);

        String path = "/primitiveBody/string";

        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), "string", new HashMap<String, String>(), null, "application/yaml", null, new String[0]);
        assertEquals(str, "\"string\"");
    }

    @Test
    public void verifyStringPostBodyWithJsonContentType() throws Exception {
        client.setDebugging(true);

        String path = "/primitiveBody/string";

        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), "string", new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "\"string\"");
    }

    @Test
    public void verifyBinaryPostBody() throws Exception {
        client.setDebugging(true);

        String path = "/primitiveBody/binary";

        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), new byte[]{42, 0, 1}, new
                HashMap<String, String>(), null, MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_OCTET_STREAM, new String[0]);
        assertEquals(str.getBytes(), new byte[]{42, 0, 1});
    }

    @Test
    public void verifyPostFormData() throws Exception {
        String path = "/formTest";

        MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
        formData.add("user", "tony,the tam");

        String str = client.invokeAPI(
            path,               // path
            "POST",             // method
            new HashMap<String, String>(),  // query
            null,               // body
            new HashMap<String, String>(), // header
            Entity.form(formData),         // form
            "application/json", // accept
            "x-www-form-urlencoded",  // contentType
            new String[0]);

        assertEquals(str, "tony,the tam");
    }

    @Test
    public void verifyMissingRequiredPostBody() throws Exception {
        String path = "/primitiveBody/inline";

        try {
            String str = client.invokeAPI(
                    path,                           // path
                    "POST",                         // method
                    new HashMap<String, String>(),  // query
                    null,                           // body
                    new HashMap<String, String>(),  // header
                    null,                           // form
                    "application/json",             // accept
                    "application/json",             // contentType
                    new String[0]);

            System.out.println(str);
        }
        catch (ApiException e) {
            // expected!
            assertTrue(e.getCode() == 400);
        }
    }

    @Test
    public void verifyRequiredPostBody() throws Exception {
        String path = "/primitiveBody/inline";

        String str = client.invokeAPI(
                path,                           // path
                "POST",                         // method
                new HashMap<String, String>(),  // query
                new Object(),                            // body
                new HashMap<String, String>(),  // header
                null,                           // form
                "application/json",             // accept
                "application/json",             // contentType
                new String[0]);

        assertEquals(str,"success!");
    }

    @Test
    public void verifyInvalidPostException() throws Exception {
        String path = "/unmappedWithModel/3";

        try {
            String str = client.invokeAPI(
                    path,                           // path
                    "POST",                         // method
                    new HashMap<String, String>(),  // query
                    "BAD BAD BAD!!>><<<{{[[",       // body
                    new HashMap<String, String>(),  // header
                    null,                           // form
                    "application/json",             // accept
                    "application/json",             // contentType
                    new String[0]);
            fail("should have thrown an exception!");
        }
        catch (ApiException e) {
            assertTrue(e.getCode() == 400);
        }
    }


    @Test
    public void verifyResponseHeaders() throws Exception {
        String path = "/responseHeaders";

        try {
            String str = client.invokeAPI(
                    path,                           // path
                    "GET",                          // method
                    new HashMap<String, String>(),  // query
                    null,                           // body
                    new HashMap<String, String>(),  // header
                    null,                           // form
                    "application/json",             // accept
                    "application/json",             // contentType
                    new String[0]);

            fail("should have thrown an exception!");
        }
        catch (ApiException e) {
            Object o = e.getResponseHeaders().get("foo");
            assertEquals(o.toString(), "[bar]");
        }
    }
}
