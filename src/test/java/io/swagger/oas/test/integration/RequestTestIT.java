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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
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
    public void verifyOperationWithDisabledInputValidation() throws Exception {
        String path = "/operationWithDisabledValidation";

        Map<String,String> body = new HashMap<>();
        body.put("username","Bob");
        // missing the required `id` property
        Response response = client.getResponse(path, "POST", new HashMap<String, String>(), body, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void verifyHidden() throws Exception {
        String path = "/hidden";
        Response response = client.getResponse(path, "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        //assertEquals(response.getStatus(), 404);
    }


    @Test
    public void verifyOperationWithDisabledOutputValidation() throws Exception {
        String path = "/operationWithDisabledValidation";
        Response response = client.getResponse(path, "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void verifyUpdatePetWithCookies() throws Exception {
        String path = "/pet";

        Map<String,String> body = new HashMap<>();
        body.put("id", "10");
        body.put("name","Cat");
        Response response = client.getResponse(path, "PUT", new HashMap<String, String>(), body, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertNotNull(response.getCookies());
        assertEquals(response.getCookies().size(), 1);
        NewCookie cookie = response.getCookies().get("type");
        assertEquals(cookie.getName(), "type");
        assertEquals(cookie.getValue(), "chocolate");
    }

    @Test
    public void verifyUpdatePetWithNullBody() throws Exception {
        String path = "/pets";
        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "OK!");
    }

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
    public void verifyNotSupport() throws Exception {
        Map<String,String> body = new HashMap<>();
        body.put("pet_type","Cat");

        String path = "/pets";
        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), body, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "OK!");
    }

    @Test
    public void verifyNotSupportByFailling() throws Exception {
        Map<String,Integer> body = new HashMap<>();
        body.put("pet_type",31);

        String path = "/pets";
        try {
            client.invokeAPI(path, "POST", new HashMap<String, String>(), body, new HashMap<String, String>(), null, "application/json", null, new String[0]);
            Assert.fail("No exception was thrown");
        } catch (ApiException e) {
            Assert.assertEquals(e.getCode(), HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    @Test
    public void verifyArrayModelMapping() throws Exception {
        final Address first = new Address();
        first.setStreet("first");
        final Address second = new Address();
        second.setStreet("second");
        String str = client.invokeAPI("/withModelArray/3", "POST", new HashMap<String, String>(),
                Arrays.asList(first, second), new HashMap<String, String>(), null,
                "application/json", null, new String[0]);
        assertEquals(str,"[{\"street\":\"first\"},{\"street\":\"second\"}]");

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
    public void verifyBinaryBytePostBody() throws Exception {
        client.setDebugging(true);

        String path = "/primitiveBody/binary";

        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), new byte[]{42, 0, 1} , new
                HashMap<String, String>(), null, MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_OCTET_STREAM, new String[0]);

        assertEquals(str.getBytes(), new byte[]{42, 0, 1} );
    }

    @Test
    public void verifyBinaryStreamPostBody() throws Exception {
        client.setDebugging(true);

        String path = "/primitiveBody/binary";

        byte[] initialArray = new byte[]{42, 0, 1};
        ByteArrayInputStream targetStream = new ByteArrayInputStream(initialArray);

        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), targetStream , new
                HashMap<String, String>(), null, MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_OCTET_STREAM, new String[0]);

        assertEquals(str.getBytes(), new byte[]{42, 0, 1});
    }

    @Test
    public void verifyBinaryFilePostBody() throws Exception {
        client.setDebugging(true);

        String path = "/primitiveBody/binary";


        File file = File.createTempFile("inflector-test2-", ".tmp");
        PrintWriter writer = new PrintWriter(file);
        writer.println("The first line");
        writer.println("The second line");
        writer.close();

        file = new File(file.getPath());


        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), file , new
                HashMap<String, String>(), null, MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_OCTET_STREAM, new String[0]);

        assertEquals(str, FileUtils.readFileToString(file,"utf-8"));
    }

    @Test
    public void verifyTextPlainMediaTypeBody() throws Exception {
        client.setDebugging(true);

        String path = "/multipleMediaType";

        String str = client.invokeAPI(path, "POST", new HashMap<String, String>(), "string", new HashMap<String, String>(), null, "text/plain", "text/plain", new String[0]);
        assertEquals(str, "");
    }

    @Test
    public void verifyMultipleMediaTypeBody() throws Exception {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();

        formData.add("id","1");
        formData.add("name","coky");
        formData.add("dogType","chiguagua");

        String path = "/multipleMediaType";

        String str = client.invokeAPI(
                path,               // path
                "POST",             // method
                new HashMap<>(),  // query
                null,               // body
                new HashMap<String, String>(), // header
                Entity.form(formData),         // form
                "application/json", // accept
                "application/x-www-form-urlencoded",  // contentType
                new String[0]);

        assertEquals(str, "{\"id\":1,\"name\":\"coky\"}");
    }

    @Test
    public void verifyPostFormData() throws Exception {
        String path = "/formTest";

        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("user", "tony,the tam");

        String str = client.invokeAPI(
            path,               // path
            "POST",             // method
            new HashMap<>(),  // query
            null,               // body
            new HashMap<String, String>(), // header
            Entity.form(formData),         // form
            "application/json", // accept
            "application/x-www-form-urlencoded",  // contentType
            new String[0]);

        assertEquals(str, "tony,the tam");
    }

    @Test
    public void verifyPostFormDataInBody() throws Exception {
        String path = "/post";

        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("custname", "Grace");
        formData.add("custtel", "+12345444");
        formData.add("custemail", "grace@gmail.com");
        formData.add("size", "large");
        formData.add("topping", "bacon");
        formData.add("delivery", "20:30");
        formData.add("comments", "Fast!");

        String str = client.invokeAPI(
                path,               // path
                "POST",             // method
                new HashMap<>(),  // query
                null,               // body
                new HashMap<String, String>(), // header
                Entity.form(formData),         // form
                "application/json", // accept
                "application/x-www-form-urlencoded",  // contentType
                new String[0]);

        assertEquals(str, "Grace");
    }

    @Test
    public void verifyPostFormDataInBodyWithNullValues() throws Exception {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();

        formData.add("id","1");
        formData.add("name","");
        formData.add("dogType","chiguagua");

        String path = "/multipleMediaTypeForNullValues";

        String str = client.invokeAPI(
                path,               // path
                "POST",             // method
                new HashMap<>(),  // query
                null,               // body
                new HashMap<>(), // header
                Entity.form(formData),         // form
                "application/json", // accept
                "application/x-www-form-urlencoded",  // contentType
                new String[0]);

        assertEquals(str, "{\"id\":1,\"dogType\":\"chiguagua\"}");
    }

    @Test
    public void verifyPostFormDataInBodyWithComplexValues() throws Exception {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();

        formData.add("id","10");
        formData.add("name","doggie");
        formData.add("category","{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"Dogs\"\n" +
                "}");
        formData.add("photoUrls", "x,y,z");
        formData.add("tags","{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"Tag1\"\n" +
                "},{\n" +
                "  \"id\": 2,\n" +
                "  \"name\": \"Tag2\"\n" +
                "}");
        formData.add("status", "available");

        String path = "/multipleMediaTypeWithComplexValues";

        String str = client.invokeAPI(
                path,               // path
                "POST",             // method
                new HashMap<>(),  // query
                null,               // body
                new HashMap<>(), // header
                Entity.form(formData),         // form
                "application/json", // accept
                "application/x-www-form-urlencoded",  // contentType
                new String[0]);

        assertEquals(str, "{\"id\":10,\"category\":{\"id\":1,\"name\":\"Dogs\"}," +
                "\"name\":\"doggie\",\"photoUrls\":[\"x\",\"y\",\"z\"]," +
                "\"tags\":[{\"id\":1,\"name\":\"Tag1\"},{\"id\":2,\"name\":\"Tag2\"}],\"status\":\"available\"}");
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
