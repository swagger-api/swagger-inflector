package io.swagger.test.integration.responses;

import static org.testng.Assert.assertEquals;
import io.swagger.test.client.ApiClient;
import io.swagger.util.Json;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ComplexResponseTestIT {
  ApiClient client = new ApiClient();
  /**
   * verifies that the return value generates a schema
   */
  @Test
  public void verifyGetComplexResponse() throws Exception {
    Map<String, String> queryParams = new HashMap<String, String>();

    String str = client.invokeAPI("/mockResponses/complexResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
    ObjectMapper mapper = Json.create().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    assertEquals(mapper.readValue(str, JsonNode.class), 
      mapper.readValue("{\"street\":\"12345 El Monte Road\",\"city\":\"Los Altos Hills\",\"state\":\"CA\",\"zip\":\"94022\"}", JsonNode.class));
  }

  /**
   * verifies that the return value uses the schema example
   */
  @Test
  public void verifyGetComplexResponseWithExample() throws Exception {
    Map<String, String> queryParams = new HashMap<String, String>();

    String str = client.invokeAPI("/mockResponses/complexResponseWithExample", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
    ObjectMapper mapper = Json.create().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    assertEquals(mapper.readValue(str, JsonNode.class), mapper.readValue("{\n  \"foo\":\"bar\"\n}\n", JsonNode.class));
  }

  /**
   * verifies that the return value uses the schema example in an array
   */
  @Test
  public void complexArrayResponse() throws Exception {
    Map<String, String> queryParams = new HashMap<String, String>();

    String str = client.invokeAPI("/mockResponses/complexArrayResponse", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
    ObjectMapper mapper = Json.create().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    
    assertEquals(mapper.readValue(str, JsonNode.class),
      mapper.readValue("[{\"city\":\"Los Altos Hills\",\"state\":\"CA\",\"street\":\"12345 El Monte Road\",\"zip\":\"94022\"}]", JsonNode.class));
  }

  /**
   * verifies that the return value uses the schema example in an array
   */
  @Test
  public void complexArrayResponseWithExample() throws Exception {
    Map<String, String> queryParams = new HashMap<String, String>();

    String str = client.invokeAPI("/mockResponses/complexArrayResponseWithExample", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
    ObjectMapper mapper = Json.create().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    assertEquals(mapper.readValue(str, JsonNode.class),
      mapper.readValue("[{\"foo\":\"bar\"}]", JsonNode.class));
  }
}
