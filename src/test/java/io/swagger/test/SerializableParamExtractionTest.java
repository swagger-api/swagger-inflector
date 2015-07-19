package io.swagger.test;

import static org.testng.Assert.*;
import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.models.Operation;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.StringProperty;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class SerializableParamExtractionTest {
  ReflectionUtils utils = new ReflectionUtils();

  @Test
  public void getMethodGenerationNameTest() throws Exception {
    Operation operation = new Operation();
    String methodName = utils.getMethodName("/foo/bar", "GET", operation);

    assertEquals(methodName, "fooBarGET");
  }

  @Test
  public void getMethodNameWithOperationIDTest() throws Exception {
    Operation operation = new Operation().operationId("superFun");
    String methodName = utils.getMethodName("/foo/bar", "GET", operation);

    assertEquals(methodName, "superFun");
  }

  @Test
  public void getStringParameterClassTest() throws Exception {
    Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new StringProperty()), null);
    assertEquals(cls, String.class);
  }

  @Test
  public void getIntegerParameterClassTest() throws Exception {
    Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new IntegerProperty()), null);
    assertEquals(cls, Integer.class);
  }

  @Test
  public void getLongParameterClassTest() throws Exception {
    Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new LongProperty()), null);
    assertEquals(cls, Long.class);
  }

  @Test
  public void getFloatParameterClassTest() throws Exception {
    Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new FloatProperty()), null);
    assertEquals(cls, Float.class);
  }

  @Test
  public void getDoubleParameterClassTest() throws Exception {
    Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new DoubleProperty()), null);
    assertEquals(cls, Double.class);
  }

  @Test
  public void getBooleanParameterClassTest() throws Exception {
    Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new BooleanProperty()), null);
    assertEquals(cls, Boolean.class);
  }

  @Test
  public void getDateParameterClassTest() throws Exception {
    Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new DateProperty()), null);
    assertEquals(cls, LocalDate.class);
  }

  @Test
  public void getDateTimeParameterClassTest() throws Exception {
    Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new DateTimeProperty()), null);
    assertEquals(cls, DateTime.class);
  }

  @Test
  public void getStringArrayParameterClassTest() throws Exception {
    Class<?> cls = utils.getParameterSignature(new QueryParameter()
      .property(new ArrayProperty(new StringProperty())), null);
    assertEquals(cls, List.class);
  }  
}