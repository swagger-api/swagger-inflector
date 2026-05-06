package io.swagger.oas.inflector.schema;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.core.util.Json;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SchemaValidatorTest {

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
}
