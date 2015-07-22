package io.swagger.sample.integration.responses;

import io.swagger.models.*;
import io.swagger.sample.client.ApiClient;

import io.swagger.util.Json;
import io.swagger.util.Yaml;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.*;
import java.math.BigDecimal;

import org.testng.annotations.Test;

public class ComplexResponseTestIT {
  ApiClient client = new ApiClient();
  /**
   * verifies that the return value generates a schema
   */
  @Test
  public void verifyGetComplexResponse() throws Exception {
    Map<String, String> queryParams = new HashMap<String, String>();

    String str = client.invokeAPI("/mockResponses/complexResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
    assertEquals(str, "{\"city\":\"Los Altos Hills\",\"state\":\"CA\",\"street\":\"12345 El Monte Road\",\"zip\":\"94022\"}");
  }

  /**
   * verifies that the return value uses the schema example
   */
  @Test
  public void verifyGetComplexResponseWithExample() throws Exception {
    Map<String, String> queryParams = new HashMap<String, String>();

    String str = client.invokeAPI("/mockResponses/complexResponseWithExample", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
    assertEquals(str, "{\n  \"foo\":\"bar\"\n}\n");
  }

  /**
   * verifies that the return value uses the schema example in an array
   */
  @Test
  public void complexArrayResponse() throws Exception {
    Map<String, String> queryParams = new HashMap<String, String>();

    String str = client.invokeAPI("/mockResponses/complexArrayResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
    assertEquals(str, "[{\"city\":\"Los Altos Hills\",\"state\":\"CA\",\"street\":\"12345 El Monte Road\",\"zip\":\"94022\"}]");
  }

  /**
   * verifies that the return value uses the schema example in an array
   */
  @Test
  public void complexArrayResponseWithExample() throws Exception {
    Map<String, String> queryParams = new HashMap<String, String>();

    String str = client.invokeAPI("/mockResponses/complexArrayResponseWithExample", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
    assertEquals(str, "[{\n  \"foo\":\"bar\"\n}\n]");
  }
}
