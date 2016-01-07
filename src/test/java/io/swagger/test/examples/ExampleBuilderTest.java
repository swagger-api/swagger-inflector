/*
 *  Copyright 2016 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.swagger.test.examples;

import com.fasterxml.jackson.databind.module.SimpleModule;

import io.swagger.converter.ModelConverters;
import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.XmlExampleSerializer;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.processors.JsonExampleDeserializer;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Xml;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.test.models.User;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

public class ExampleBuilderTest {
    static {
        // register the JSON serializer
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonNodeExampleSerializer());
        Json.mapper().registerModule(simpleModule);
        Yaml.mapper().registerModule(simpleModule);
    }

    @Test
    public void testReadModel() throws Exception {
        Map<String, Model> definitions = ModelConverters.getInstance().readAll(User.class);
        Object o = ExampleBuilder.fromProperty(new RefProperty("User"), definitions);

        String str = new XmlExampleSerializer().serialize((Example) o);
        assertEquals(str, "<?xml version='1.1' encoding='UTF-8'?><user><id>0</id><user>string</user><child><childNames>string</childNames></child></user>");
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
        assertEquals(xmlString, "<?xml version='1.1' encoding='UTF-8'?><user><userName>fehguy</userName><addressess><address><street>12345 El Monte Blvd</street><city>Los Altos Hills</city><state>CA</state><zip>94022</zip></address></addressess><managers><key>key</key><value>SVP Engineering</value></managers><kidsAges>9</kidsAges></user>");
        assertEquals(Yaml.pretty().writeValueAsString(rep), "---\nusername: \"fehguy\"\naddresses:\n- street: \"12345 El Monte Blvd\"\n  city: \"Los Altos Hills\"\n  state: \"CA\"\n  zip: \"94022\"\nmanagers:\n  key: \"key\"\n  value: \"SVP Engineering\"\nkidsAges:\n- 9\n");
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
        simpleModule.addSerializer(new JsonNodeExampleSerializer());
        simpleModule.addDeserializer(Example.class, new JsonExampleDeserializer());
        Json.mapper().registerModule(simpleModule);

        Example rep = (Example) ExampleBuilder.fromProperty(new StringProperty("hello").example("fun"), definitions);
        assertEquals(Json.pretty(rep), "\"fun\"");
    }

    @Test
    public void testXmlExample() throws Exception {
        Model model = new ModelImpl()
          .property("id", new StringProperty()
            .xml(new Xml()
              .name("fred")));

        Map<String, Model> definitions = new HashMap<String, Model>();
        definitions.put("User", model);
        assertEquals(Json.pretty(ExampleBuilder.fromProperty(new RefProperty("User"), definitions)), "{\n  \"id\" : \"string\"\n}");
    }

    @Test
    public void testXmlBoolean() throws Exception {
        BooleanProperty sp = new BooleanProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEquals(xmlString, "<?xml version='1.1' encoding='UTF-8'?><boolean>true</boolean>");
    }

    @Test
    public void testXmlDecimal() throws Exception {
        DecimalProperty sp = new DecimalProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEquals(xmlString, "<?xml version='1.1' encoding='UTF-8'?><decimal>1.5</decimal>");
    }

    @Test
    public void testXmlFloat() throws Exception {
        FloatProperty sp = new FloatProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEquals(xmlString, "<?xml version='1.1' encoding='UTF-8'?><float>1.1</float>");
    }

    @Test
    public void testXmlInteger() throws Exception {
        IntegerProperty sp = new IntegerProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEquals(xmlString, "<?xml version='1.1' encoding='UTF-8'?><integer>0</integer>");
    }

    @Test
    public void testXmlLong() throws Exception {
        LongProperty sp = new LongProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEquals(xmlString, "<?xml version='1.1' encoding='UTF-8'?><long>0</long>");
    }

    @Test
    public void testXmlString() throws Exception {
        StringProperty sp = new StringProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEquals(xmlString, "<?xml version='1.1' encoding='UTF-8'?><string>string</string>");
    }

    @Test
    public void testRecursiveModel() throws Exception {
        Model person = new ModelImpl()
          .property(
            "age",
            new IntegerProperty()
            .example(42))
          .property(
            "spouse",
            new RefProperty("Person"));

        Map<String, Model> definitions = new HashMap<String, Model>();
        definitions.put("Person", person);

        Example rep = (Example) ExampleBuilder.fromProperty(new RefProperty("Person"), definitions);
        assertEquals(Json.pretty(rep), "{\n  \"age\" : 42\n}");
        String xmlString = new XmlExampleSerializer().serialize(rep);
        assertEquals(xmlString, "<?xml version='1.1' encoding='UTF-8'?><Person><age>42</age></Person>");
    }
}