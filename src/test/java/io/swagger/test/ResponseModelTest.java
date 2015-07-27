package io.swagger.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import io.swagger.converter.ModelConverters;
import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.models.IntegerExample;
import io.swagger.inflector.examples.models.ObjectExample;
import io.swagger.inflector.examples.models.StringExample;
import io.swagger.models.Model;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.test.models.User;
import io.swagger.util.Json;

import java.util.Map;

import org.testng.annotations.Test;

public class ResponseModelTest {
  @Test
  public void testConvertStringProperty() throws Exception {
    StringProperty p = new StringProperty();
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof StringExample);
    assertEquals(((StringExample) o).textValue(), "string");
  }

  @Test
  public void testConvertStringPropertyWithExample() throws Exception {
    StringProperty p = new StringProperty()
      .example("fun");
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof StringExample);
    assertEquals(((StringExample) o).textValue(), "fun");
  }

  @Test
  public void testConvertIntegerProperty() throws Exception {
    IntegerProperty p = new IntegerProperty();
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof IntegerExample);
    assertEquals(((IntegerExample) o).asInt(), new Integer(0));
  }

  @org.junit.Test
  public void testConvertIntegerPropertyWithExample() throws Exception {
    IntegerProperty p = new IntegerProperty()
      .example(3);

    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof IntegerExample);
    assertEquals(((IntegerExample) o).asInt(), new Integer(3));
  }

  @Test
  public void testComplexModel() throws Exception {
    RefProperty p = new RefProperty("User");
    Map<String, Model> definitions = ModelConverters.getInstance().readAll(User.class);
    Object o = ExampleBuilder.fromProperty(p, definitions);

    ObjectExample n = Json.mapper().convertValue(o, ObjectExample.class);
    assertNotNull(n);
  }
}