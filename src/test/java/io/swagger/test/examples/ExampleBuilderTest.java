package io.swagger.test.examples;

import static org.junit.Assert.*;
import io.swagger.converter.ModelConverters;
import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.XmlExampleSerializer;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.processors.JsonExampleDeserializer;
import io.swagger.inflector.processors.JsonExampleSerializer;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Xml;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.test.models.User;
import io.swagger.util.Json;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class ExampleBuilderTest {
  static {
    // register the JSON serializer
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(new JsonExampleSerializer());
    Json.mapper().registerModule(simpleModule);
  }

  @org.junit.Test
  public void testReadModel() throws Exception {
    Map<String, Model> definitions = ModelConverters.getInstance().readAll(User.class);
    Object o = ExampleBuilder.fromProperty(new RefProperty("User"), definitions);
    Json.prettyPrint(o);
    
    String str = new XmlExampleSerializer().serialize((Example)o);
    System.out.println(str);
  }
  
  @Test
  public void testXmlJackson() throws Exception {
    Model model = new ModelImpl()
      .xml(new Xml()
          .name("user"))
      .property(
        "username",
        new StringProperty()
          .example("fehguy")
          .xml(new Xml()
            .name("userName")))
      .property("addresses", new ArrayProperty()
          .xml(new Xml().wrapped(true))
          .items(new RefProperty("Address")))
      .property("managers", new MapProperty()
        .additionalProperties(new StringProperty().example("SVP Engineering")))
      .property("kidsAges", new ArrayProperty()
        .items(new IntegerProperty().example(9)));
  
    Map<String, Model> definitions = new HashMap<String, Model>();
    definitions.put("User", model);
    
    Model address = new ModelImpl()
      .xml(new Xml()
        .name("address"))
      .property(
        "street",
        new StringProperty()
          .example("12345 El Monte Blvd"))
      .property(
        "city",
        new StringProperty()
          .example("Los Altos Hills"))
      .property("state", new StringProperty()
        .example("CA")
        .minLength(2)
        .maxLength(2))
      .property("zip", new StringProperty()
        .example("94022"));

    definitions.put("Address", address);

    Example rep = (Example) ExampleBuilder.fromProperty(new RefProperty("User"), definitions);

    String xmlString = new XmlExampleSerializer().serialize(rep);
    System.out.println(xmlString);
    Json.prettyPrint(rep);
  }

  @Test
  public void testComplexArray() throws Exception {
    Map<String, Model> definitions = new HashMap<String, Model>();
    
    Model address = new ModelImpl()
      .xml(new Xml()
        .name("address"))
      .property(
        "street",
        new StringProperty()
          .example("12345 El Monte Blvd"))
      .property(
        "city",
        new StringProperty()
          .example("Los Altos Hills"))
      .property("state", new StringProperty()
        .example("CA")
        .minLength(2)
        .maxLength(2))
      .property("zip", new StringProperty()
        .example("94022"));

    definitions.put("Address", address);

    Example rep = (Example) ExampleBuilder.fromProperty(new ArrayProperty(new RefProperty("Address")), definitions);
    
    String json = Json.pretty(rep);
    
    assertEquals("[ {\n  \"street\" : \"12345 El Monte Blvd\",\n  \"city\" : \"Los Altos Hills\",\n  \"state\" : \"CA\",\n  \"zip\" : \"94022\"\n} ]", json);
  }

  @Test
  public void testComplexArrayWithExample() throws Exception {
    Map<String, Model> definitions = new HashMap<String, Model>();
    
    Model address = new ModelImpl()
      .example("{\"foo\":\"bar\"}")
      .xml(new Xml()
        .name("address"))
      .property(
        "street",
        new StringProperty()
          .example("12345 El Monte Blvd"))
      .property(
        "city",
        new StringProperty()
          .example("Los Altos Hills"))
      .property("state", new StringProperty()
        .example("CA")
        .minLength(2)
        .maxLength(2))
      .property("zip", new StringProperty()
        .example("94022"));

    definitions.put("Address", address);
    
    // register the JSON serializer
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(new JsonExampleSerializer());
    simpleModule.addDeserializer(Example.class, new JsonExampleDeserializer());
    Json.mapper().registerModule(simpleModule);

//    Example rep = (Example) ExampleBuilder.fromProperty(new ArrayProperty(new RefProperty("Address")), definitions);

//    Json.prettyPrint(rep);
    
    Example rep = (Example)ExampleBuilder.fromProperty(new StringProperty("hello").example("fun"), definitions);
    Json.prettyPrint(rep);
    
//    assertEquals("[{\"street\":\"12345 El Monte Road\",\"city\":\"Los Altos Hills\",\"state\":\"CA\",\"zip\":\"94022\"}]", json);
  }

  @Test
  public void testXmlExample() throws Exception {
    Model model = new ModelImpl()
      .property(
        "id",
        new StringProperty()
          .xml(new Xml()
            .name("fred")));
    
    Map<String, Model> definitions = new HashMap<String, Model>();
    definitions.put("User", model);
    Json.prettyPrint(ExampleBuilder.fromProperty(new RefProperty("User"), definitions));
  }
}