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

import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.models.Example;
import io.swagger.test.client.ApiClient;
import io.swagger.test.client.ApiException;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.io.CharSource.wrap;
import static jdk.nashorn.internal.parser.JSONParser.quote;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class PrimitiveResponseTestIT {
    ApiClient client = new ApiClient();

    /**
     * empty response
     */
    @org.junit.Test
    public void verifyGetEmptyResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("user", "tony");

        String str = client.invokeAPI("/mockResponses", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "");
    }

    /**
     * verifies that the return value is automatically created
     */
    @org.junit.Test
    public void verifyGetFloatResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveFloatResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, String.valueOf(ExampleBuilder.SAMPLE_FLOAT_PROPERTY_VALUE));
    }

    /**
     * verifies that the return value is automatically created
     */
    @org.junit.Test
    public void verifyGetDoubleResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveDoubleResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, String.valueOf(ExampleBuilder.SAMPLE_DOUBLE_PROPERTY_VALUE));
    }

    /**
     * verifies that the return value is automatically created
     */
    @org.junit.Test
    public void verifyGetUUIDResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveUUIDResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, quote(ExampleBuilder.SAMPLE_UUID_PROPERTY_VALUE));
    }

    /**
     * verifies that the return value is automatically created
     */
    @org.junit.Test
    public void verifyGetStringResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveStringResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, quote(ExampleBuilder.SAMPLE_STRING_PROPERTY_VALUE));
    }

    /**
     * verifies that the return value uses the schema example
     */
    @org.junit.Test
    public void verifyGetStringResponseWithExample() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveStringResponseWithExample", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "\"fun\"");
    }

    /**
     * verifies that the date return value is automatically created
     */
    @org.junit.Test
    public void verifyGetDateResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveDateResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, quote(ExampleBuilder.SAMPLE_DATE_PROPERTY_VALUE));
    }

    /**
     * verifies that the date-time return value is automatically created
     */
    @org.junit.Test
    public void verifyGetDateTimeResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveDateTimeResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, quote(ExampleBuilder.SAMPLE_DATETIME_PROPERTY_VALUE));
    }

    /**
     * verifies that the BigDecimal return value is automatically created
     */
    @org.junit.Test
    public void verifyGetBigDecimalResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveBigDecimalResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, String.valueOf(ExampleBuilder.SAMPLE_DECIMAL_PROPERTY_VALUE ));
    }

    /**
     * verifies that the email return value is automatically created
     */
    @org.junit.Test
    public void verifyGetEmailResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveEmailResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, quote(ExampleBuilder.SAMPLE_EMAIL_PROPERTY_VALUE));
    }

    /**
     * verifies that the email return value is automatically created
     */
    @org.junit.Test
    public void verifyGetMapResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiaveMapResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "{\"key\":\"key\",\"value\":\"string\"}");
    }

    /**
     * test for https://github.com/swagger-api/swagger-inflector/issues/125
     */
    @org.junit.Test
    public void verifyBaseIntegerResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/issue-125", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, String.valueOf(ExampleBuilder.SAMPLE_BASE_INTEGER_PROPERTY_VALUE));
    }

    /**
     * test for https://github.com/swagger-api/swagger-inflector/issues/128
     */
    @org.junit.Test
    public void verify303Response() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        try {
            String str = client.invokeAPI("/issue-128", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
            fail("expected non-200 response");
        }
        catch (ApiException e) {
            assertTrue(e.getCode() == 303);
            assertEquals(e.getMessage(), "\"http://foo.bar/other\"");
        }
    }
}
