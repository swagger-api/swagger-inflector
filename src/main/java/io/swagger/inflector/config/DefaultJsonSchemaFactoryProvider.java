package io.swagger.inflector.config;

import com.github.fge.jsonschema.main.JsonSchemaFactory;

public final class DefaultJsonSchemaFactoryProvider implements JsonSchemaFactoryProvider {

  public JsonSchemaFactory get() {
    return JsonSchemaFactory.byDefault();
  }
  
}
