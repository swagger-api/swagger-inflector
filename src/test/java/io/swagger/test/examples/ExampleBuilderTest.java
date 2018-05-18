/*
 *  Copyright 2017 SmartBear Software
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
import io.swagger.inflector.examples.models.AbstractExample;
import io.swagger.inflector.examples.models.ArrayExample;
import io.swagger.inflector.examples.models.DoubleExample;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.examples.models.ObjectExample;
import io.swagger.inflector.examples.models.StringExample;
import io.swagger.inflector.processors.JsonExampleDeserializer;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.inflector.utils.ResolverUtil;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.Xml;
import io.swagger.models.properties.AbstractProperty;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.test.models.User;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

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
        assertEqualsIgnoreLineEnding(str, "<?xml version='1.1' encoding='UTF-8'?><user><id>0</id><user>string</user><children><child>string</child></children></user>");
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

        Example rep = ExampleBuilder.fromProperty(new RefProperty("User"), definitions);

        String xmlString = new XmlExampleSerializer().serialize(rep);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><user><userName>fehguy</userName><addressess><address><street>12345 El Monte Blvd</street><city>Los Altos Hills</city><state>CA</state><zip>94022</zip></address></addressess><managers><key>key</key><value>SVP Engineering</value></managers><kidsAges>9</kidsAges></user>");
        assertEqualsIgnoreLineEnding(Yaml.pretty().writeValueAsString(rep), "---\nusername: \"fehguy\"\naddresses:\n- street: \"12345 El Monte Blvd\"\n  city: \"Los Altos Hills\"\n  state: \"CA\"\n  zip: \"94022\"\nmanagers:\n  key: \"key\"\n  value: \"SVP Engineering\"\nkidsAges:\n- 9\n");
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

        assertEqualsIgnoreLineEnding(json,"[ {\n  \"street\" : \"12345 El Monte Blvd\",\n  \"city\" : \"Los Altos Hills\",\n  \"state\" : \"CA\",\n  \"zip\" : \"94022\"\n} ]");
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
        assertEqualsIgnoreLineEnding(Json.pretty(rep), "\"fun\"");
    }

    @Test
    public void testXmlExample() throws Exception {
        Model model = new ModelImpl()
          .property("id", new StringProperty()
            .xml(new Xml()
              .name("fred")));

        Map<String, Model> definitions = new HashMap<String, Model>();
        definitions.put("User", model);
        assertEqualsIgnoreLineEnding(Json.pretty(ExampleBuilder.fromProperty(new RefProperty("User"), definitions)), "{\n  \"id\" : \"string\"\n}");
    }

    @Test
    public void testXmlBoolean() throws Exception {
        BooleanProperty sp = new BooleanProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><boolean>true</boolean>");
    }

    @Test
    public void testXmlDecimal() throws Exception {
        DecimalProperty sp = new DecimalProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><decimal>1.5</decimal>");
    }

    @Test
    public void testXmlFloat() throws Exception {
        FloatProperty sp = new FloatProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><float>1.1</float>");
    }

    @Test
    public void testXmlInteger() throws Exception {
        IntegerProperty sp = new IntegerProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><integer>0</integer>");
    }

    @Test
    public void testXmlLong() throws Exception {
        LongProperty sp = new LongProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><long>0</long>");
    }

    @Test
    public void testXmlString() throws Exception {
        StringProperty sp = new StringProperty();
        Example ex = ExampleBuilder.fromProperty(sp, null);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><string>string</string>");
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
        assertEqualsIgnoreLineEnding(Json.pretty(rep), "{\n  \"age\" : 42\n}");
        String xmlString = new XmlExampleSerializer().serialize(rep);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><Person><age>42</age></Person>");
    }

    @Test
    public void testEmptyStringArrayJsonModel() throws Exception {
        ArrayExample example = new ArrayExample();
        example.add(new StringExample());
        assertEquals(Json.pretty(example), "[ null ]");
    }

    @Test
    public void testEmptyDoubleArrayJsonModel() throws Exception {
        ArrayExample example = new ArrayExample();
        example.add(new DoubleExample());
        assertEquals(Json.pretty(example), "[ 4.56 ]");
    }

    @Test
    public void testDoubleArrayModelAsString() throws Exception {
        ArrayExample example = new ArrayExample();
        example.add(new DoubleExample());
        assertEquals(example.asString(), "[4.56]");
    }

    @Test
    public void testEmptyArrayXmlModel() throws Exception {
        ArrayExample example = new ArrayExample();
        example.add(new StringExample("test"));
        String xmlString = new XmlExampleSerializer().serialize(example);

        // array of empty primitives makes little sense in xml - but lets be nice...
        Assert.assertEquals(xmlString, "<?xml version='1.1' encoding='UTF-8'?><string>test</string>");
    }

    @Test
    public void testIssue126Simple() throws Exception {
        String schema =
            "{\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"name\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"example\": \"hi!?\"\n" +
            "    }\n" +
            "  }\n" +
            "}";
        Model model = Json.mapper().readValue(schema, Model.class);

        Map<String, Model> definitions = new HashMap<>();
        definitions.put("SimpleModel", model);

        Example rep = ExampleBuilder.fromProperty(new RefProperty("SimpleModel"), definitions);

        assertEqualsIgnoreLineEnding(Json.pretty(rep),
            "{\n" +
            "  \"name\" : \"hi!?\"\n" +
            "}");
    }

    @Test
    public void testIssue126Composed() throws Exception {
        String schema =
            "{\n" +
            "  \"allOf\": [\n" +
            "    {\n" +
            "      \"type\": \"object\",\n" +
            "      \"properties\": {\n" +
            "        \"id\": {\n" +
            "          \"type\": \"integer\",\n" +
            "          \"format\": \"int32\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"object\",\n" +
            "      \"properties\": {\n" +
            "        \"name\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"example\": \"hi!?\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        Model model = Json.mapper().readValue(schema, Model.class);

        Map<String, Model> definitions = new HashMap<>();
        definitions.put("ComposedModel", model);

        Example rep = ExampleBuilder.fromProperty(new RefProperty("ComposedModel"), definitions);

        assertEqualsIgnoreLineEnding(Json.pretty(rep),
            "{\n" +
            "  \"id\" : 0,\n" +
            "  \"name\" : \"hi!?\"\n" +
            "}");
    }

    @Test
    public void testRecursiveSchema() throws Exception {
        String schema = "{\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"id\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"circular1\": {\n" +
            "      \"$ref\": \"#/definitions/Circular\"\n" +
            "    },\n" +
            "    \"circular2\": {\n" +
            "      \"$ref\": \"#/definitions/Circular\"\n" +
            "    }\n" +
            "  }\n" +
            "}";
        Model model = Json.mapper().readValue(schema, Model.class);

        Map<String, Model> definitions = new HashMap<>();
        definitions.put("Circular", model);

        Example rep = ExampleBuilder.fromProperty(new RefProperty("Circular"), definitions);

        assertEqualsIgnoreLineEnding(Json.pretty(rep), "{\n" +
            "  \"id\" : \"string\",\n" +
            "  \"circular1\" : { },\n" +
            "  \"circular2\" : { }\n" +
            "}");
    }

    @Test
    public void testIssue126Inline() throws Exception {
        String schema =
            "{\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"id\": {\n" +
            "      \"type\": \"integer\",\n" +
            "      \"format\": \"int32\",\n" +
            "      \"example\": 999\n" +
            "    },\n" +
            "    \"inline\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"properties\": {\n" +
            "        \"first\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"last\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
        Model model = Json.mapper().readValue(schema, Model.class);

        Map<String, Model> definitions = new HashMap<>();
        definitions.put("InlineModel", model);

        Example rep = ExampleBuilder.fromProperty(new RefProperty("InlineModel"), definitions);

        assertEqualsIgnoreLineEnding(Json.pretty(rep), "{\n" +
            "  \"id\" : 999,\n" +
            "  \"inline\" : {\n" +
            "    \"first\" : \"string\",\n" +
            "    \"last\" : \"string\"\n" +
            "  }\n" +
            "}");
    }

    @Test
    public void testIssue133() throws Exception {
        IntegerProperty integerProperty = new IntegerProperty();
        integerProperty.setFormat("int64");
        integerProperty.setExample(new Long(4321));
        Model model = new ModelImpl()
            .property("int64", integerProperty);

        Map<String, Model> definitions = new HashMap<>();
        definitions.put("Address", model);

        Example rep = ExampleBuilder.fromProperty(new RefProperty("Address"), definitions);
        assertEqualsIgnoreLineEnding(Json.pretty(rep),
                "{\n" +
                "  \"int64\" : 4321\n" +
                "}");
    }

    @Test
    public void testIssue127() throws Exception {
        IntegerProperty integerProperty = new IntegerProperty();
        integerProperty.setFormat(null);
        integerProperty.setExample(new Long(4321));
        Model model = new ModelImpl()
                .property("unboundedInteger", integerProperty);

        Map<String, Model> definitions = new HashMap<>();
        definitions.put("Address", model);

        Example rep = ExampleBuilder.fromProperty(new RefProperty("Address"), definitions);

        Json.prettyPrint(rep);
        assertEqualsIgnoreLineEnding(Json.pretty(rep),
                "{\n" +
                "  \"unboundedInteger\" : 4321\n" +
                "}");
    }

    @Test
    public void testInvalidExample() throws Exception {
        testInvalidExample( new IntegerProperty(), "asd",
            ExampleBuilder.SAMPLE_INT_PROPERTY_VALUE, 123 );

        testInvalidExample( new LongProperty(), "asd",
            ExampleBuilder.SAMPLE_LONG_PROPERTY_VALUE, 123 );

        testInvalidExample( new FloatProperty(), "asd",
            ExampleBuilder.SAMPLE_FLOAT_PROPERTY_VALUE, 2.1f );

        testInvalidExample( new DoubleProperty(), "asd",
            ExampleBuilder.SAMPLE_DOUBLE_PROPERTY_VALUE, 3.1f );

        // base types that don't implement setting a sample value
        testInvalidExample( new DecimalProperty(), "asd",
            ExampleBuilder.SAMPLE_DECIMAL_PROPERTY_VALUE );

        testInvalidExample( new BaseIntegerProperty(), "asd",
            ExampleBuilder.SAMPLE_BASE_INTEGER_PROPERTY_VALUE );
    }

    public void testInvalidExample(AbstractProperty property, String invalidValue, Object defaultValue ) throws Exception {
       testInvalidExample( property, invalidValue, defaultValue, null );
    }

    public void testInvalidExample(AbstractProperty property, String invalidValue, Object defaultValue, Object sampleValue ) throws Exception {
        property.setExample( invalidValue);

        Model model = new ModelImpl().property("test", property );

        Map<String, Model> definitions = new HashMap<>();
        definitions.put("Test", model);

        // validate that the internal default value is returned if an invalid value is set
        ObjectExample rep = (ObjectExample) ExampleBuilder.fromProperty(new RefProperty("Test"), definitions);
        AbstractExample example = (AbstractExample) rep.get( "test" );
        assertEquals( String.valueOf(defaultValue), example.asString() );

        // validate that a specified default value is returned if an invalid value is set
        if( sampleValue != null ) {
            property.setDefault(String.valueOf(sampleValue));
            rep = (ObjectExample) ExampleBuilder.fromProperty(new RefProperty("Test"), definitions);
            example = (AbstractExample) rep.get("test");
            assertEquals(String.valueOf(sampleValue), example.asString());
        }
    }

    private void assertEqualsIgnoreLineEnding(String actual, String expected) {
        assertEquals(actual.replace("\r\n", "\n"), expected);
    }

    @Test
    public void testObjectsWithAnonymousObjectArrays() throws Exception {
        Swagger swagger = new SwaggerParser().read("src/test/swagger/issue-171.yaml");

        String output = getExampleForPath(swagger, "/test");

        assertEqualsIgnoreLineEnding(output, "[ {\n" +
                "  \"id\" : \"string\",\n" +
                "  \"nestedArray\" : [ {\n" +
                "    \"id\" : \"string\",\n" +
                "    \"name\" : \"string\"\n" +
                "  } ]\n" +
                "} ]");


        Response response = swagger.getPath("/anothertest").getGet().getResponses().get( "200" );
        Example example = ExampleBuilder.fromProperty(response.getSchema(), swagger.getDefinitions());

        output = new XmlExampleSerializer().serialize(example);
        assertEquals( output, "<?xml version='1.1' encoding='UTF-8'?><string>string</string>");
    }

    @Test
    public void testEnumExample() throws Exception {
        Swagger swagger = new SwaggerParser().read("src/test/swagger/issue-171.yaml");

        String output = getExampleForPath(swagger, "/color");
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"color\" : \"black\"\n" +
                "}");
    }

    @Test
    public void testIssue1261InlineSchemaExample() throws Exception {
        Swagger swagger = new SwaggerParser().read("src/test/swagger/issue-1261.yaml");

        String output = getExampleForPath(swagger, "/user");
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"id\" : 42,\n" +
                "  \"name\" : \"Arthur Dent\"\n" +
                "}");
    }

    @Test
    public void testIssue1177RefArrayExample() throws Exception {
        Swagger swagger = new SwaggerParser().read("src/test/swagger/issue-1177.yaml");

        String output = getExampleForPath(swagger, "/array");
        assertEqualsIgnoreLineEnding(output, "[ \"string\" ]");
    }

    @Test
    public void testInlinedArrayExample() throws Exception {
        Swagger swagger = new SwaggerParser().read("src/test/swagger/array-example.yaml");

        String output = getExampleForPath(swagger, "/");
        assertEqualsIgnoreLineEnding(output, "[ {\n" +
                "  \"id\" : 1,\n" +
                "  \"name\" : \"Arthur Dent\"\n" +
                "}, {\n" +
                "  \"id\" : 2,\n" +
                "  \"name\" : \"Ford Prefect\"\n" +
                "} ]");
    }

    @Test
    public void testIssue1263SchemaExampleNestedObjects() throws Exception {
        Swagger swagger = new SwaggerParser().read("src/test/swagger/issue-1263.yaml");

        String output = getExampleForPath(swagger, "/nested_object");
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"nested_object\" : {\n" +
                "    \"foo\" : \"bar\"\n" +
                "  }\n" +
                "}");
    }

    @Test
    public void testDifferentExampleTypes() throws Exception {
        Swagger swagger = new SwaggerParser().read("src/test/swagger/example-types.yaml");

        String output = getExampleForPath(swagger, "/user");
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"obj\" : {\n" +
                "    \"b\" : \"ho\",\n" +
                "    \"a\" : \"hey\"\n" +
                "  },\n" +
                "  \"arr\" : [ \"hey\", \"ho\" ],\n" +
                "  \"double\" : 1.2,\n" +
                "  \"int\" : 42,\n" +
                "  \"biginteger\" : 118059162071741130342442,\n" +
                "  \"long\" : 1099511627776,\n" +
                "  \"boolean\" : true,\n" +
                "  \"string\" : \"Arthur Dent\"\n" +
                "}");
    }

    @Test
    public void testAllOfAndRef() throws Exception {
        Swagger swagger = new SwaggerParser().read("src/test/swagger/allOfAndRef.yaml");

        String output = getExampleForPath(swagger, "/refToAllOf");
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"username\" : \"trillian\",\n" +
                "  \"id\" : 4\n" +
                "}");
    }

    @Test
    public void testCircularRefSchema() throws Exception {
        Swagger swagger = new SwaggerParser().read("./src/test/swagger/circuler-refs-SPLAT-56.yaml");
        ResolverUtil resolverUtil = new ResolverUtil();
        resolverUtil.resolveFully(swagger);
        ExampleBuilder.fromProperty(new RefProperty("Source"), resolverUtil.getResolvedModels());
    }

    private String getExampleForPath(Swagger swagger, String s) {
        Response response = swagger.getPath(s).getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromProperty(response.getSchema(), swagger.getDefinitions());
        return Json.pretty(example);
    }
}
