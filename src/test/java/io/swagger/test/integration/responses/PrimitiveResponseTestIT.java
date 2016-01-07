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

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

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
        assertEquals(str, "1.1");
    }

    /**
     * verifies that the return value is automatically created
     */
    @org.junit.Test
    public void verifyGetDoubleResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveDoubleResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "1.23");
    }

    /**
     * verifies that the return value is automatically created
     */
    @org.junit.Test
    public void verifyGetUUIDResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveUUIDResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"");
    }

    /**
     * verifies that the return value is automatically created
     */
    @org.junit.Test
    public void verifyGetStringResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveStringResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "\"string\"");
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
        assertEquals(str, "\"2015-07-20\"");
    }

    /**
     * verifies that the date-time return value is automatically created
     */
    @org.junit.Test
    public void verifyGetDateTimeResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveDateTimeResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "\"2015-07-20T15:49:04-07:00\"");
    }

    /**
     * verifies that the BigDecimal return value is automatically created
     */
    @org.junit.Test
    public void verifyGetBigDecimalResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveBigDecimalResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "1.5");
    }

    /**
     * verifies that the email return value is automatically created
     */
    @org.junit.Test
    public void verifyGetEmailResponse() throws Exception {
        Map<String, String> queryParams = new HashMap<String, String>();

        String str = client.invokeAPI("/mockResponses/primitiveEmailResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        assertEquals(str, "\"apiteam@swagger.io\"");
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
}
