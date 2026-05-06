package io.swagger.oas.inflector.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import io.swagger.v3.core.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SchemaValidator {
    static Map<String, Schema> SCHEMA_CACHE = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidator.class);

    // OAS 3.0 uses JSON Schema Draft-4 (with boolean exclusiveMin/Max)
    private static final SchemaRegistry REGISTRY_DRAFT_4 = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4);
    // OAS 3.1 uses JSON Schema Draft 2020-12 natively
    private static final SchemaRegistry REGISTRY_DRAFT_2020_12 = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);

    public enum Direction {
        INPUT,
        OUTPUT
    }

    public enum OpenApiVersion {
        V3_0,  // OpenAPI 3.0.x - uses Draft-04 (nullable, boolean exclusiveMin/Max)
        V3_1   // OpenAPI 3.1.x - uses Draft 2020-12 natively
    }

    private static volatile OpenApiVersion openApiVersion = OpenApiVersion.V3_0;

    public static void setOpenApiVersion(String version) {
        if (version != null && version.startsWith("3.1")) {
            openApiVersion = OpenApiVersion.V3_1;
            LOGGER.info("OpenAPI 3.1 detected - using JSON Schema Draft 2020-12");
        } else {
            openApiVersion = OpenApiVersion.V3_0;
            LOGGER.info("OpenAPI 3.0 detected - using JSON Schema Draft-04");
        }
        SCHEMA_CACHE.clear();
    }

    public static OpenApiVersion getOpenApiVersion() {
        return openApiVersion;
    }

    public static boolean validate(Object argument, String schema, Direction direction) {
        try {
            JsonNode content = Json.mapper().convertValue(argument, JsonNode.class);
            SchemaRegistry registry = getRegistry();

            // For OAS 3.0, handle nullable (not in any JSON Schema draft)
            String processedSchema = openApiVersion == OpenApiVersion.V3_0
                    ? convertNullableForDraft04(schema)
                    : schema;

            Schema jsonSchema = registry.getSchema(processedSchema, InputFormat.JSON);

            List<Error> errors = jsonSchema.validate(content.toString(), InputFormat.JSON);
            if (!errors.isEmpty()) {
                if (direction.equals(Direction.INPUT)) {
                    LOGGER.warn("input: {}\ndoes not match schema: \n{}", content, schema);
                } else {
                    LOGGER.warn("response: {}\ndoes not match schema: \n{}", content, schema);
                }
                for (Error error : errors) {
                    LOGGER.warn("  validation error: {}", error.getMessage());
                }
            }
            return errors.isEmpty();
        } catch (Exception e) {
            LOGGER.error("can't validate model against schema", e);
        }

        return true;
    }

    public static Schema getValidationSchema(String schema) {
        schema = schema.trim();

        Schema output = SCHEMA_CACHE.get(schema);

        if (output == null) {
            try {
                SchemaRegistry registry = getRegistry();
                String processedSchema = openApiVersion == OpenApiVersion.V3_0
                        ? convertNullableForDraft04(schema)
                        : schema;

                Schema jsonSchema = registry.getSchema(processedSchema, InputFormat.JSON);
                SCHEMA_CACHE.put(schema, jsonSchema);
                output = jsonSchema;
            } catch (Exception e) {
                LOGGER.error("can't parse schema: {}", schema, e);
            }
        }
        return output;
    }

    private static SchemaRegistry getRegistry() {
        return openApiVersion == OpenApiVersion.V3_0 ? REGISTRY_DRAFT_4 : REGISTRY_DRAFT_2020_12;
    }

    /**
     * Converts OAS 3.0 'nullable' keyword to type array for Draft-04 validation.
     * Draft-04 natively handles boolean exclusiveMinimum/exclusiveMaximum,
     * but 'nullable' is an OAS extension not present in any JSON Schema draft.
     */
    static String convertNullableForDraft04(String schema) {
        try {
            JsonNode node = Json.mapper().readTree(schema);
            if (node.isObject()) {
                convertNullableInNode((ObjectNode) node);
                return Json.mapper().writeValueAsString(node);
            }
        } catch (Exception e) {
            LOGGER.debug("Schema processing failed, using original: {}", e.getMessage());
        }
        return schema;
    }

    private static void convertNullableInNode(ObjectNode node) {
        JsonNode nullableNode = node.get("nullable");
        JsonNode typeNode = node.get("type");

        if (nullableNode != null && nullableNode.isBoolean()) {
            node.remove("nullable");

            if (nullableNode.asBoolean() && typeNode != null) {
                // nullable: true → add "null" to type array
                if (typeNode.isTextual()) {
                    ArrayNode typeArray = Json.mapper().createArrayNode();
                    typeArray.add(typeNode.asText());
                    typeArray.add("null");
                    node.set("type", typeArray);
                } else if (typeNode.isArray()) {
                    ArrayNode typeArray = (ArrayNode) typeNode;
                    boolean hasNull = false;
                    for (JsonNode t : typeArray) {
                        if (t.isTextual() && "null".equals(t.asText())) {
                            hasNull = true;
                            break;
                        }
                    }
                    if (!hasNull) {
                        typeArray.add("null");
                    }
                }
            }
        }

        // Recurse into nested schemas
        JsonNode properties = node.get("properties");
        if (properties != null && properties.isObject()) {
            for (Map.Entry<String, JsonNode> entry : properties.properties()) {
                if (entry.getValue().isObject()) {
                    convertNullableInNode((ObjectNode) entry.getValue());
                }
            }
        }

        JsonNode items = node.get("items");
        if (items != null && items.isObject()) {
            convertNullableInNode((ObjectNode) items);
        }

        JsonNode additionalProperties = node.get("additionalProperties");
        if (additionalProperties != null && additionalProperties.isObject()) {
            convertNullableInNode((ObjectNode) additionalProperties);
        }

        for (String keyword : new String[]{"allOf", "anyOf", "oneOf"}) {
            JsonNode composite = node.get(keyword);
            if (composite != null && composite.isArray()) {
                for (JsonNode subSchema : composite) {
                    if (subSchema.isObject()) {
                        convertNullableInNode((ObjectNode) subSchema);
                    }
                }
            }
        }

        JsonNode notNode = node.get("not");
        if (notNode != null && notNode.isObject()) {
            convertNullableInNode((ObjectNode) notNode);
        }
    }
}
