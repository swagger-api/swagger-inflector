package io.swagger.oas.inflector.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Schema;
import io.swagger.v3.core.util.Json;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SchemaValidatorTest {

    @BeforeMethod
    public void resetState() {
        SchemaValidator.setOpenApiVersion("3.0.0");
    }

    // --- convertNullableForDraft04 ---

    @Test
    public void nullableStringBecomesTypeArray() throws Exception {
        String result = SchemaValidator.convertNullableForDraft04("{\"type\":\"string\",\"nullable\":true}");
        JsonNode node = Json.mapper().readTree(result);
        assertFalse(node.has("nullable"));
        assertTrue(node.get("type").isArray());
        assertEquals(node.get("type").size(), 2);
        assertEquals(node.get("type").get(0).asText(), "string");
        assertEquals(node.get("type").get(1).asText(), "null");
    }

    @Test
    public void nullableExistingTypeArrayAddsNull() throws Exception {
        String result = SchemaValidator.convertNullableForDraft04("{\"type\":[\"string\",\"integer\"],\"nullable\":true}");
        JsonNode node = Json.mapper().readTree(result);
        assertFalse(node.has("nullable"));
        assertEquals(node.get("type").size(), 3);
        assertEquals(node.get("type").get(2).asText(), "null");
    }

    @Test
    public void nullableTypeArrayAlreadyHasNullNotDuplicated() throws Exception {
        String result = SchemaValidator.convertNullableForDraft04("{\"type\":[\"string\",\"null\"],\"nullable\":true}");
        JsonNode node = Json.mapper().readTree(result);
        assertFalse(node.has("nullable"));
        assertEquals(node.get("type").size(), 2);
    }

    @Test
    public void nullableFalseRemovesKeywordOnly() throws Exception {
        String result = SchemaValidator.convertNullableForDraft04("{\"type\":\"string\",\"nullable\":false}");
        JsonNode node = Json.mapper().readTree(result);
        assertFalse(node.has("nullable"));
        assertTrue(node.get("type").isTextual());
        assertEquals(node.get("type").asText(), "string");
    }

    @Test
    public void noNullableKeywordPassesThrough() throws Exception {
        String schema = "{\"type\":\"string\",\"minLength\":1}";
        String result = SchemaValidator.convertNullableForDraft04(schema);
        JsonNode node = Json.mapper().readTree(result);
        assertFalse(node.has("nullable"));
        assertEquals(node.get("type").asText(), "string");
        assertEquals(node.get("minLength").asInt(), 1);
    }

    @Test
    public void nestedPropertiesNullableConverted() throws Exception {
        String schema = "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\",\"nullable\":true}}}";
        String result = SchemaValidator.convertNullableForDraft04(schema);
        JsonNode node = Json.mapper().readTree(result);
        JsonNode nameNode = node.get("properties").get("name");
        assertFalse(nameNode.has("nullable"));
        assertTrue(nameNode.get("type").isArray());
    }

    @Test
    public void itemsNullableConverted() throws Exception {
        String schema = "{\"type\":\"array\",\"items\":{\"type\":\"string\",\"nullable\":true}}";
        String result = SchemaValidator.convertNullableForDraft04(schema);
        JsonNode node = Json.mapper().readTree(result);
        JsonNode items = node.get("items");
        assertFalse(items.has("nullable"));
        assertTrue(items.get("type").isArray());
    }

    @Test
    public void nullableWithoutTypeRemovesKeywordOnly() throws Exception {
        String result = SchemaValidator.convertNullableForDraft04("{\"nullable\":true,\"$ref\":\"#/components/schemas/Foo\"}");
        JsonNode node = Json.mapper().readTree(result);
        assertFalse(node.has("nullable"));
        assertFalse(node.has("type"));
    }

    @Test
    public void allOfSubschemasNullableConverted() throws Exception {
        String schema = "{\"allOf\":[{\"type\":\"string\",\"nullable\":true}]}";
        String result = SchemaValidator.convertNullableForDraft04(schema);
        JsonNode node = Json.mapper().readTree(result);
        JsonNode sub = node.get("allOf").get(0);
        assertFalse(sub.has("nullable"));
        assertTrue(sub.get("type").isArray());
    }

    // --- setOpenApiVersion / getOpenApiVersion ---

    @Test
    public void version30SetsV30() {
        SchemaValidator.setOpenApiVersion("3.0.3");
        assertEquals(SchemaValidator.getOpenApiVersion(), SchemaValidator.OpenApiVersion.V3_0);
    }

    @Test
    public void version31SetsV31() {
        SchemaValidator.setOpenApiVersion("3.1.0");
        assertEquals(SchemaValidator.getOpenApiVersion(), SchemaValidator.OpenApiVersion.V3_1);
    }

    @Test
    public void nullVersionDefaultsToV30() {
        SchemaValidator.setOpenApiVersion(null);
        assertEquals(SchemaValidator.getOpenApiVersion(), SchemaValidator.OpenApiVersion.V3_0);
    }

    // --- validate() ---

    @Test
    public void validStringPassesV30() {
        SchemaValidator.setOpenApiVersion("3.0.3");
        assertTrue(SchemaValidator.validate("hello", "{\"type\":\"string\"}", SchemaValidator.Direction.INPUT));
    }

    @Test
    public void invalidTypeFails() {
        SchemaValidator.setOpenApiVersion("3.0.3");
        assertFalse(SchemaValidator.validate(42, "{\"type\":\"string\"}", SchemaValidator.Direction.INPUT));
    }

    @Test
    public void nullableFieldAcceptsNullV30() {
        SchemaValidator.setOpenApiVersion("3.0.3");
        assertTrue(SchemaValidator.validate(null, "{\"type\":\"string\",\"nullable\":true}", SchemaValidator.Direction.INPUT));
    }

    @Test
    public void validStringPassesV31() {
        SchemaValidator.setOpenApiVersion("3.1.0");
        assertTrue(SchemaValidator.validate("hello", "{\"type\":\"string\"}", SchemaValidator.Direction.INPUT));
    }

    @Test
    public void invalidTypeFailsV31() {
        SchemaValidator.setOpenApiVersion("3.1.0");
        assertFalse(SchemaValidator.validate(42, "{\"type\":\"string\"}", SchemaValidator.Direction.INPUT));
    }

    // --- getValidationSchema() / cache ---

    @Test
    public void getValidationSchemaReturnsCachedInstance() {
        SchemaValidator.setOpenApiVersion("3.0.3");
        String schema = "{\"type\":\"string\"}";
        Schema first = SchemaValidator.getValidationSchema(schema);
        Schema second = SchemaValidator.getValidationSchema(schema);
        assertSame(first, second);
    }

    @Test
    public void versionChangeClearsCache() {
        SchemaValidator.setOpenApiVersion("3.0.3");
        String schema = "{\"type\":\"string\"}";
        SchemaValidator.SCHEMA_CACHE.put(schema, SchemaValidator.getValidationSchema(schema));
        assertFalse(SchemaValidator.SCHEMA_CACHE.isEmpty());

        SchemaValidator.setOpenApiVersion("3.1.0");
        assertTrue(SchemaValidator.SCHEMA_CACHE.isEmpty());
    }
}
