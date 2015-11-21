package io.swagger.test.integration.responses;

import io.swagger.test.client.ApiClient;
import io.swagger.test.client.ApiException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class SchemaResponseIT {
    private final ApiClient client = new ApiClient();

    @Test
    public void testValidResponse() throws Exception {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("street", "12345");
        client.invokeAPI("/withModel/3", "POST", new HashMap<String, String>(), body,
                new HashMap<String, String>(), null, null, null, new String[0]);
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
}
