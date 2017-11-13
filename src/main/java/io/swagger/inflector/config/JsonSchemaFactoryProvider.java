package io.swagger.inflector.config;

import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * A provider of JsonSchemaFactory instances used to configure JSON Schema validation.
 */
public interface JsonSchemaFactoryProvider {

  /**
   * Returns the JSON schema factory to be usd for validation. The
   * @return the JsonSchemaFactory
   */
  JsonSchemaFactory get();

}
