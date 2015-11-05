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

package io.swagger.test.integration;

import io.swagger.test.client.ApiClient;
import io.swagger.test.client.ApiException;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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
        assertEquals(str, "ok");
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


    @org.junit.Test
    public void verifyStringPostBody() throws Exception {
        client.setDebugging(true);

        String path = "/primitiveBody/string";

        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), "string", new HashMap<String, String>(), null, "application/yaml", null, new String[0]);
        assertEquals(str, "string");
    }

    @Test
    public void verifyPostFormData() throws Exception {
        String path = "/formTest";

        MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
        formData.add("user", "tony");

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

        assertEquals(str, "tony");
    }
}