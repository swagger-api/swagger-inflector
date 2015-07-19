package io.swagger.test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.EmailProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.UUIDProperty;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.Test;

public class ValueCoercionTest {
  ReflectionUtils utils = new ReflectionUtils();

  @Test
  public void testConvertStringValue() throws Exception {
    List<String> values = Arrays.asList("a");
    
    Parameter parameter = new QueryParameter().items(new StringProperty());
    Object o = utils.cast(values, parameter, String.class, null);
    
    assertTrue(o instanceof String);
  }

  @Test
  public void testConvertIntegerValue() throws Exception {
    List<String> values = Arrays.asList("1");

    Parameter parameter = new QueryParameter().items(new IntegerProperty());
    Object o = utils.cast(values, parameter, Integer.class, null);
    
    assertTrue(o instanceof Integer);
  }

  @Test
  public void testConvertInvalidIntegerValue() throws Exception {
    List<String> values = Arrays.asList("1abczdf");

    Parameter parameter = new QueryParameter().items(new IntegerProperty());
    Object o = utils.cast(values, parameter, Integer.class, null);
    
    assertNull(o);
  }

  @Test
  public void testConvertLongValue() throws Exception {
    List<String> values = Arrays.asList("1");

    Parameter parameter = new QueryParameter().items(new LongProperty());
    Object o = utils.cast(values, parameter, Long.class, null);
    
    assertTrue(o instanceof Long);
  }

  @Test
  public void testConvertInvalidLongValue() throws Exception {
    List<String> values = Arrays.asList("1zzzzz");

    Parameter parameter = new QueryParameter().items(new LongProperty());
    Object o = utils.cast(values, parameter, Long.class, null);
    
    assertNull(o);
  }
  
  @Test
  public void testConvertFloatValue() throws Exception {
    List<String> values = Arrays.asList("1");

    Parameter parameter = new QueryParameter().items(new FloatProperty());
    Object o = utils.cast(values, parameter, Float.class, null);
    
    assertTrue(o instanceof Float);
  }
  
  @Test
  public void testConvertInvalidFloatValue() throws Exception {
    List<String> values = Arrays.asList("1;;lkaj;lasjkdfs");

    Parameter parameter = new QueryParameter().items(new FloatProperty());
    Object o = utils.cast(values, parameter, Float.class, null);
    
    assertNull(o);
  }
  
  @Test
  public void testConvertDoubleValue() throws Exception {
    List<String> values = Arrays.asList("1");

    Parameter parameter = new QueryParameter().items(new DoubleProperty());
    Object o = utils.cast(values, parameter, Double.class, null);
    
    assertTrue(o instanceof Double);
  }
  
  @Test
  public void testConvertInvalidDoubleValue() throws Exception {
    List<String> values = Arrays.asList("abcdefg");

    Parameter parameter = new QueryParameter().items(new DoubleProperty());
    Object o = utils.cast(values, parameter, Double.class, null);
    
    assertNull(o);
  }

  @Test
  public void testConvertBooleanValue() throws Exception {
    List<String> values = Arrays.asList("true");

    Parameter parameter = new QueryParameter().items(new BooleanProperty());
    Object o = utils.cast(values, parameter, Boolean.class, null);
    
    assertTrue(o instanceof Boolean);
  }

  @Test
  public void testConvertBooleanValue1() throws Exception {
    List<String> values = Arrays.asList("1");

    Parameter parameter = new QueryParameter().items(new BooleanProperty());
    Object o = utils.cast(values, parameter, Boolean.class, null);
    
    assertTrue(o instanceof Boolean);
    assertTrue((Boolean)o);
  }

  @Test
  public void testConvertBooleanValue2() throws Exception {
    List<String> values = Arrays.asList("0");

    Parameter parameter = new QueryParameter().items(new BooleanProperty());
    Object o = utils.cast(values, parameter, Boolean.class, null);
    
    assertTrue(o instanceof Boolean);
    assertFalse((Boolean)o);
  }

  @Test
  public void testConvertUUIDValue() throws Exception {
    List<String> values = Arrays.asList("163e1000-2a5a-4be2-b271-3470b63dff00");

    Parameter parameter = new QueryParameter().items(new UUIDProperty());
    Object o = utils.cast(values, parameter, UUID.class, null);
    
    assertTrue(o instanceof UUID);
  }

  @Test
  public void testConvertInvalidUUIDValue() throws Exception {
    List<String> values = Arrays.asList("bleh");

    Parameter parameter = new QueryParameter().items(new UUIDProperty());
    Object o = utils.cast(values, parameter, UUID.class, null);
    
    assertNull(o);
  }
  
  @Test
  public void testConvertEmailValue() throws Exception {
    List<String> values = Arrays.asList("fehguy@gmail.com");

    Parameter parameter = new QueryParameter().items(new EmailProperty());
    Object o = utils.cast(values, parameter, String.class, null);
    
    assertTrue(o instanceof String);
  }
}
