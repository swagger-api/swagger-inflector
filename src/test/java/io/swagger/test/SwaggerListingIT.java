package io.swagger.test;

import static org.testng.Assert.assertNotNull;
import io.swagger.models.Swagger;
import io.swagger.test.client.ApiClient;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

import java.util.HashMap;

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
