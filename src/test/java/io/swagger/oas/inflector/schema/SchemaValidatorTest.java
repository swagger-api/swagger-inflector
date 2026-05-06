package io.swagger.oas.inflector.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Schema;
import io.swagger.v3.core.util.Json;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SchemaValidatorTest {

    private static final SchemaValidator.OpenApiVersion V30 = SchemaValidator.OpenApiVersion.V3_0;
    private static final SchemaValidator.OpenApiVersion V31 = SchemaValidator.parseOpenApiVersion("3.1.0");

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
    public void nullableWithoutTypeRemovesKeywordOnly() throws Exception {
        String result = SchemaValidator.convertNullableForDraft04("{\"nullable\":true,\"$ref\":\"#/components/schemas/Foo\"}");
        JsonNode node = Json.mapper().readTree(result);
        assertFalse(node.has("nullable"));
        assertFalse(node.has("type"));
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
    public void allOfSubschemasNullableConverted() throws Exception {
        String schema = "{\"allOf\":[{\"type\":\"string\",\"nullable\":true}]}";
        String result = SchemaValidator.convertNullableForDraft04(schema);
        JsonNode node = Json.mapper().readTree(result);
        JsonNode sub = node.get("allOf").get(0);
        assertFalse(sub.has("nullable"));
        assertTrue(sub.get("type").isArray());
    }

    // --- parseOpenApiVersion ---

    @Test
    public void version30ParsesAsV30() {
        assertEquals(SchemaValidator.parseOpenApiVersion("3.0.3"), SchemaValidator.OpenApiVersion.V3_0);
    }

    @Test
    public void version31ParsesAsV31() {
        assertEquals(SchemaValidator.parseOpenApiVersion("3.1.0"), SchemaValidator.OpenApiVersion.V3_1);
    }

    @Test
    public void nullVersionParsesAsV30() {
        assertEquals(SchemaValidator.parseOpenApiVersion(null), SchemaValidator.OpenApiVersion.V3_0);
    }

    // --- validate() ---

    @Test
    public void validStringPassesV30() {
        assertTrue(SchemaValidator.validate("hello", "{\"type\":\"string\"}", SchemaValidator.Direction.INPUT, V30));
    }

    @Test
    public void invalidTypeFailsV30() {
        assertFalse(SchemaValidator.validate(42, "{\"type\":\"string\"}", SchemaValidator.Direction.INPUT, V30));
    }

    @Test
    public void nullableFieldAcceptsNullV30() {
        assertTrue(SchemaValidator.validate(null, "{\"type\":\"string\",\"nullable\":true}", SchemaValidator.Direction.INPUT, V30));
    }

    @Test
    public void validStringPassesV31() {
        assertTrue(SchemaValidator.validate("hello", "{\"type\":\"string\"}", SchemaValidator.Direction.INPUT, V31));
    }

    @Test
    public void invalidTypeFailsV31() {
        assertFalse(SchemaValidator.validate(42, "{\"type\":\"string\"}", SchemaValidator.Direction.INPUT, V31));
    }

    @Test
    public void defaultOverloadUsesV30() {
        assertTrue(SchemaValidator.validate("hello", "{\"type\":\"string\"}", SchemaValidator.Direction.INPUT));
    }

    // --- getValidationSchema() / cache ---

    @Test
    public void getValidationSchemaReturnsCachedInstance() {
        String schema = "{\"type\":\"string\"}";
        Schema first = SchemaValidator.getValidationSchema(schema, V30);
        Schema second = SchemaValidator.getValidationSchema(schema, V30);
        assertSame(first, second);
    }

    @Test
    public void differentVersionsProduceDifferentCacheEntries() {
        String schema = "{\"type\":\"string\"}";
        Schema from30 = SchemaValidator.getValidationSchema(schema, V30);
        Schema from31 = SchemaValidator.getValidationSchema(schema, V31);
        assertNotNull(from30);
        assertNotNull(from31);
        assertNotSame(from30, from31);
    }
}
