package io.swagger.test;

import io.swagger.util.Yaml;

import org.testng.annotations.*;

import static org.testng.Assert.*;

public class ConfigurationLoaderTest {
  @Test
  public void testLoadSampleConfiguration() throws Exception {
    System.setProperty("config", "src/test/config/sample1.yaml");
    io.swagger.inflector.config.Configuration config = io.swagger.inflector.config.Configuration.read();
    Yaml.prettyPrint(config);
  }
}
