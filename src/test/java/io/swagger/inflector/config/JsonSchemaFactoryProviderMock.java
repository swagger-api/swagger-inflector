package io.swagger.inflector.config;

import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class JsonSchemaFactoryProviderMock implements JsonSchemaFactoryProvider {
  public JsonSchemaFactory jsonSchemaFactory;

  @Override
  public JsonSchemaFactory get() {
    return jsonSchemaFactory = JsonSchemaFactory.byDefault();
  }
}
