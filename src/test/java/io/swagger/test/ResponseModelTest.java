package io.swagger.test;

import io.swagger.inflector.utils.ExampleBuilder;
import io.swagger.models.properties.*;

import org.testng.annotations.*;

import static org.testng.Assert.*;

public class ResponseModelTest {
  @Test
  public void testConvertStringProperty() throws Exception {
    StringProperty p = new StringProperty();
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof String);
    assertEquals((String) o, "string");
  }

  @Test
  public void testConvertStringPropertyWithExample() throws Exception {
    StringProperty p = new StringProperty()
      .example("fun");
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof String);
    assertEquals((String) o, "fun");
  }

  @Test
  public void testConvertIntegerProperty() throws Exception {
    IntegerProperty p = new IntegerProperty();
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof Integer);
    assertEquals((Integer) o, new Integer(0));
  }

  @Test
  public void testConvertIntegerPropertyWithExample() throws Exception {
    IntegerProperty p = new IntegerProperty()
      .example(3);
    
    Object o = ExampleBuilder.fromProperty(p, null);
    assertNotNull(o);
    assertTrue(o instanceof Integer);
    assertEquals((Integer) o, new Integer(3));
  }
 }
