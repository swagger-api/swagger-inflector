package io.swagger.inflector.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.swagger.inflector.config.Configuration;
import io.swagger.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SchemaValidator {
    static Map<String, JsonSchema> SCHEMA_CACHE = new HashMap<String, JsonSchema>();
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidator.class);
    private final String swaggerUrl;
    private JsonSchemaFactory factory;

    public enum Direction{
        INPUT,
        OUTPUT
    }

    public SchemaValidator(){
        swaggerUrl = null;
    }

    public SchemaValidator( Configuration config ) {
        swaggerUrl = config.getSwaggerUrl();
    }

    public boolean validate(Object o, String schema, Direction direction) {
        try {
            // make local refs absolute to match existing schema
            schema = schema.replaceAll("\"#\\/definitions\\/", "\"" + swaggerUrl + "#/definitions/");
            JsonNode schemaObject = Json.mapper().readTree(schema);

            JsonNode content = (o instanceof JsonNode) ? (JsonNode)o : Json.mapper().convertValue(o, JsonNode.class);
            JsonSchema jsonSchema = ensureFactory().getJsonSchema(schemaObject);

            ProcessingReport report = jsonSchema.validate(content);
            if(!report.isSuccess()) {
                if(direction.equals(Direction.INPUT)) {
                    LOGGER.warn("input: " + content.toString() + "\n" + "does not match schema: \n" + schema);
                }
                else {
                    LOGGER.warn("response: " + content.toString() + "\n" + "does not match schema: \n" + schema);
                }
            }
            return report.isSuccess();
        }
        catch (Exception e) {
            LOGGER.error("can't validate model against schema", e);
        }

        return true;
    }

    private synchronized JsonSchemaFactory ensureFactory() throws IOException {
        if( factory == null ){
            if( swaggerUrl != null ) {
                JsonNode swaggerSchemaObject = Json.mapper().readTree(new URL(swaggerUrl));

                // preload existing defintitions in schema
                factory = JsonSchemaFactory.newBuilder().setLoadingConfiguration(
                    LoadingConfiguration.newBuilder().preloadSchema(swaggerUrl, swaggerSchemaObject).freeze()
                ).freeze();
            }
            else {
                factory = JsonSchemaFactory.byDefault();
            }
        }

        return factory;
    }

    public static JsonSchema getValidationSchema(String schema) throws IOException, ProcessingException {
        schema = schema.trim();

        JsonSchema output = SCHEMA_CACHE.get(schema);

        if(output == null) {
            JsonNode schemaObject = Json.mapper().readTree(schema);
            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            com.github.fge.jsonschema.main.JsonSchema jsonSchema = factory.getJsonSchema(schemaObject);
            SCHEMA_CACHE.put(schema, jsonSchema);
            output = jsonSchema;
        }
        return output;
    }
}
