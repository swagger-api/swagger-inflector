package io.swagger.test;

import static org.testng.Assert.*;
import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

public class ArraySerializableParamExtractionTest {
  ReflectionUtils utils = new ReflectionUtils();

  @Test
  public void testConvertStringArray() throws Exception {
    List<String> values = Arrays.asList("a", "b");
    
    Parameter parameter = new QueryParameter().items(new ArrayProperty().items(new StringProperty()));
    Object o = utils.cast(values, parameter, List.class, null);

    assertTrue(o instanceof List);
    
    @SuppressWarnings("unchecked")
    List<String> objs = (List<String>)o;
    System.out.println(objs);
    assertTrue(objs.size() == 2);
    assertEquals(objs.get(0), "a");
    assertEquals(objs.get(1), "b");
  }

  @Test
  public void testConvertStringArrayCSV() throws Exception {
    List<String> values = Arrays.asList("a,b");
    
    Parameter parameter = new QueryParameter()
      .collectionFormat("csv")
      .items(new ArrayProperty()
        .items(new StringProperty()));

    Object o = utils.cast(values, parameter, List.class, null);

    assertTrue(o instanceof List);
    
    @SuppressWarnings("unchecked")
    List<String> objs = (List<String>)o;

    assertTrue(objs.size() == 2);
    assertEquals(objs.get(0), "a");
    assertEquals(objs.get(1), "b");
  }

  @Test
  public void testConvertStringArrayCSVWithEscapedValue() throws Exception {
    List<String> values = Arrays.asList("\"good, bad\",bad");
    
    Parameter parameter = new QueryParameter()
      .collectionFormat("csv")
      .items(new ArrayProperty()
        .items(new StringProperty()));

    Object o = utils.cast(values, parameter, List.class, null);

    assertTrue(o instanceof List);
    
    @SuppressWarnings("unchecked")
    List<String> objs = (List<String>)o;

    assertTrue(objs.size() == 2);
    assertEquals(objs.get(0), "good, bad");
    assertEquals(objs.get(1), "bad");
  }

  @Test
  public void testConvertStringArrayPipesWithEscapedValue() throws Exception {
    List<String> values = Arrays.asList("\"good | bad\"|bad");
    
    Parameter parameter = new QueryParameter()
      .collectionFormat("pipes")
      .items(new ArrayProperty()
        .items(new StringProperty()));

    Object o = utils.cast(values, parameter, List.class, null);

    assertTrue(o instanceof List);
    
    @SuppressWarnings("unchecked")
    List<String> objs = (List<String>)o;

    assertTrue(objs.size() == 2);
    assertEquals(objs.get(0), "good | bad");
    assertEquals(objs.get(1), "bad");
  }

  @Test
  public void testConvertStringArraySSVWithEscapedValue() throws Exception {
    List<String> values = Arrays.asList("\"good bad\" bad");
    
    Parameter parameter = new QueryParameter()
      .collectionFormat("ssv")
      .items(new ArrayProperty()
        .items(new StringProperty()));

    Object o = utils.cast(values, parameter, List.class, null);

    assertTrue(o instanceof List);
    
    @SuppressWarnings("unchecked")
    List<String> objs = (List<String>)o;

    assertTrue(objs.size() == 2);
    assertEquals(objs.get(0), "good bad");
    assertEquals(objs.get(1), "bad");
  }

  @Test
  public void testConvertIntegerArraySSVValue() throws Exception {
    List<String> values = Arrays.asList("1 2 3");
    
    Parameter parameter = new QueryParameter()
      .collectionFormat("ssv")
      .items(new ArrayProperty()
        .items(new IntegerProperty()));

    Object o = utils.cast(values, parameter, List.class, null);

    assertTrue(o instanceof List);
    
    @SuppressWarnings("unchecked")
    List<Integer> objs = (List<Integer>)o;

    assertTrue(objs.size() == 3);
    assertEquals(objs.get(0), new Integer(1));
    assertEquals(objs.get(1), new Integer(2));
    assertEquals(objs.get(2), new Integer(3));
  }

  @Test
  public void testConvertBooleanArrayCSVValue() throws Exception {
    List<String> values = Arrays.asList("true false true");
    
    Parameter parameter = new QueryParameter()
      .collectionFormat("ssv")
      .items(new ArrayProperty()
        .items(new BooleanProperty()));

    Object o = utils.cast(values, parameter, List.class, null);

    assertTrue(o instanceof List);
    
    @SuppressWarnings("unchecked")
    List<Boolean> objs = (List<Boolean>)o;

    assertTrue(objs.size() == 3);
    assertEquals(objs.get(0), Boolean.TRUE);
    assertEquals(objs.get(1), Boolean.FALSE);
    assertEquals(objs.get(2), Boolean.TRUE);
  }
}
