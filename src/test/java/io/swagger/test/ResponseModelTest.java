package io.swagger.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import io.swagger.converter.ModelConverters;
import io.swagger.inflector.utils.ExampleBuilder;
import io.swagger.models.Model;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.sample.models.User;
import io.swagger.util.Json;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

public class ResponseModelTest {
  @Test
  public void testConvertStringProperty() throws Exception {
    StringProperty p = new StringProperty();
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof TextNode);
    assertEquals(((TextNode) o).asText(), "string");
  }

  @Test
  public void testConvertStringPropertyWithExample() throws Exception {
    StringProperty p = new StringProperty()
      .example("fun");
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof TextNode);
    assertEquals(((TextNode) o).textValue(), "fun");
  }

  @Test
  public void testConvertIntegerProperty() throws Exception {
    IntegerProperty p = new IntegerProperty();
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof IntNode);
    assertEquals(((IntNode) o).asInt(), 0);
  }

  @Test
  public void testConvertIntegerPropertyWithExample() throws Exception {
    IntegerProperty p = new IntegerProperty()
      .example(3);
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof IntNode);
    assertEquals(((IntNode) o).asInt(), 3);
  }

  @Test
  public void testComplexModel() throws Exception {
    RefProperty p = new RefProperty("User");
    Map<String, Model> definitions = ModelConverters.getInstance().readAll(User.class);
    Object o = ExampleBuilder.fromProperty(p, definitions);
    
    JsonNode n = Json.mapper().convertValue(o, JsonNode.class);
    assertNotNull(n);
  }
}