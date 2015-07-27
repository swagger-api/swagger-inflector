package io.swagger.test;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

public class ConfigurationLoaderTest {
  @Test
  public void testLoadSampleConfiguration() throws Exception {
    System.setProperty("config", "src/test/config/config1.yaml");
    io.swagger.inflector.config.Configuration config = io.swagger.inflector.config.Configuration.read();
    assertNotNull(config);
  }
}
