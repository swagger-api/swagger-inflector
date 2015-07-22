package io.swagger.sample.integration;

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

public class SwaggerListingIT {
  ApiClient client = new ApiClient();

  @Test
  public void verifySwaggerJson() throws Exception {
    String str = client.invokeAPI("swagger.json", "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
    Swagger swagger = Json.mapper().readValue(str, Swagger.class);

    assertNotNull(swagger);
  }

  @Test
  public void verifySwaggerYaml() throws Exception {
    String str = client.invokeAPI("swagger.yaml", "GET", new HashMap<String, String>(), null, new HashMap<String, String>(), null, "application/yaml", null, new String[0]);
    Swagger swagger = Yaml.mapper().readValue(str, Swagger.class);

    assertNotNull(swagger);
  }
}
