package io.swagger.test;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.*;

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
  public void testConvertLongValue() throws Exception {
    List<String> values = Arrays.asList("1");

    Parameter parameter = new QueryParameter().items(new LongProperty());
    Object o = utils.cast(values, parameter, Long.class, null);
    
    assertTrue(o instanceof Long);
  }

  @Test
  public void testConvertFLoatValue() throws Exception {
    List<String> values = Arrays.asList("1");

    Parameter parameter = new QueryParameter().items(new FloatProperty());
    Object o = utils.cast(values, parameter, Float.class, null);
    
    assertTrue(o instanceof Float);
  }
  
  @Test
  public void testConvertDoubleValue() throws Exception {
    List<String> values = Arrays.asList("1");

    Parameter parameter = new QueryParameter().items(new DoubleProperty());
    Object o = utils.cast(values, parameter, Double.class, null);
    
    assertTrue(o instanceof Double);
  }
  
  @Test
  public void testConvertBooleanValue() throws Exception {
    List<String> values = Arrays.asList("true");

    Parameter parameter = new QueryParameter().items(new BooleanProperty());
    Object o = utils.cast(values, parameter, Boolean.class, null);
    
    assertTrue(o instanceof Boolean);
  }
  
  @Test
  public void testConvertUUIDValue() throws Exception {
    List<String> values = Arrays.asList("163e1000-2a5a-4be2-b271-3470b63dff00");

    Parameter parameter = new QueryParameter().items(new UUIDProperty());
    Object o = utils.cast(values, parameter, UUID.class, null);
    
    assertTrue(o instanceof UUID);
  }
  
  @Test
  public void testConvertEmailValue() throws Exception {
    List<String> values = Arrays.asList("fehguy@gmail.com");

    Parameter parameter = new QueryParameter().items(new EmailProperty());
    Object o = utils.cast(values, parameter, String.class, null);
    
    assertTrue(o instanceof String);
  }
}
