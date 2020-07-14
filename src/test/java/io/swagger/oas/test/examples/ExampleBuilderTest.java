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

package io.swagger.oas.test.examples;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.models.Swagger;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.XmlExampleSerializer;
import io.swagger.oas.inflector.examples.models.AbstractExample;
import io.swagger.oas.inflector.examples.models.ArrayExample;
import io.swagger.oas.inflector.examples.models.DoubleExample;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.examples.models.ObjectExample;
import io.swagger.oas.inflector.examples.models.StringExample;
import io.swagger.oas.inflector.processors.JsonExampleDeserializer;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.XML;

import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.oas.test.models.User;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import mockit.Injectable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

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
        Map<String, Schema> definitions = ModelConverters.getInstance().readAll(User.class);
        Object o = ExampleBuilder.fromSchema(new Schema().$ref("User"), definitions, false);

        String str = new XmlExampleSerializer().serialize((Example) o);
        assertEqualsIgnoreLineEnding(str, "<?xml version='1.1' encoding='UTF-8'?><user><id>0</id><user>string</user><children><child>string</child></children></user>");
    }

    @Test
    public void testXmlJackson() throws Exception {
        Schema model = new Schema()
                .xml(new XML()
                        .name("user"));

        Schema property1 = new StringSchema();
        property1.setName("username");
        property1.setExample("fehguy");
        property1.setXml(new XML()
                .name("userName"));

        ArraySchema property2 = new ArraySchema();
        property2.setName("addresses");
        property2.setXml(new XML().wrapped(true));
        property2.setItems(new Schema().$ref("Address"));
        Schema property3 = new Schema();
        property3.setName("managers");
        property3.setAdditionalProperties(new StringSchema().example("SVP Engineering"));
        ArraySchema property4 = new ArraySchema();
        property4.setName("kidsAges");
        property4.setItems(new IntegerSchema().example(9));

        model.addProperties("username",property1);
        model.addProperties("addresses",property2);
        model.addProperties("managers",property3);
        model.addProperties("kidsAges",property4);

        Map<String, Schema> definitions = new HashMap<>();
        definitions.put("User", model);

        Schema address = new Schema()
                .xml(new XML().name("address"));

        Schema propertyDefinition1 = new StringSchema();
        propertyDefinition1.setName("street");
        propertyDefinition1.setExample("12345 El Monte Blvd");

        Schema propertyDefinition2 = new StringSchema();
        propertyDefinition2.setName("city");
        propertyDefinition2.setExample("Los Altos Hills");

        Schema propertyDefinition3 = new StringSchema();
        propertyDefinition3.setName("state");
        propertyDefinition3.setExample("CA");
        propertyDefinition3.setMinLength(2);
        propertyDefinition3.setMaxLength(2);

        Schema propertyDefinition4 = new StringSchema();
        propertyDefinition4.setName("zip");
        propertyDefinition4.setExample("94022");

        address.addProperties("street",propertyDefinition1);
        address.addProperties("city",propertyDefinition2);
        address.addProperties("state",propertyDefinition3);
        address.addProperties("zip",propertyDefinition4);

        definitions.put("Address", address);

        Example rep = ExampleBuilder.fromSchema(new Schema().$ref("User"), definitions, false);

        String xmlString = new XmlExampleSerializer().serialize(rep);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><user><userName>fehguy</userName><addressess><address><street>12345 El Monte Blvd</street><city>Los Altos Hills</city><state>CA</state><zip>94022</zip></address></addressess><managers><additionalProp1>SVP Engineering</additionalProp1><additionalProp2>SVP Engineering</additionalProp2><additionalProp3>SVP Engineering</additionalProp3></managers><kidsAges>9</kidsAges></user>");
        assertEqualsIgnoreLineEnding(Yaml.pretty().writeValueAsString(rep),"username: fehguy\naddresses:\n- street: 12345 El Monte Blvd\n  city: Los Altos Hills\n  state: CA\n  zip: \"94022\"\nmanagers:\n  additionalProp1: SVP Engineering\n  additionalProp2: SVP Engineering\n  additionalProp3: SVP Engineering\nkidsAges:\n- 9\n");
    }

    @Test
    public void testComplexArray() throws Exception {
        Map<String, Schema> definitions = new HashMap<>();

        Schema address = new Schema()
                .xml(new XML().name("address"));

        Schema propertyDefinition1 = new StringSchema();
        propertyDefinition1.setName("street");
        propertyDefinition1.setExample("12345 El Monte Blvd");

        Schema propertyDefinition2 = new StringSchema();
        propertyDefinition2.setName("city");
        propertyDefinition2.setExample("Los Altos Hills");

        Schema propertyDefinition3 = new StringSchema();
        propertyDefinition3.setName("state");
        propertyDefinition3.setExample("CA");
        propertyDefinition3.setMinLength(2);
        propertyDefinition3.setMaxLength(2);

        Schema propertyDefinition4 = new StringSchema();
        propertyDefinition4.setName("zip");
        propertyDefinition4.setExample("94022");

        address.addProperties("street",propertyDefinition1);
        address.addProperties("city",propertyDefinition2);
        address.addProperties("state",propertyDefinition3);
        address.addProperties("zip",propertyDefinition4);

        definitions.put("Address", address);

        Example rep = (Example) ExampleBuilder.fromSchema(new ArraySchema().$ref("Address"), definitions, false);

        String json = Json.pretty(rep);

        assertEqualsIgnoreLineEnding(json,"{\n  \"street\" : \"12345 El Monte Blvd\",\n  \"city\" : \"Los Altos Hills\",\n  \"state\" : \"CA\",\n  \"zip\" : \"94022\"\n}");
    }

    @Test
    public void testComplexArrayWithExample() throws Exception {
        Map<String, Schema> definitions = new HashMap<>();

        Schema address = new Schema()
          .example("{\"foo\":\"bar\"}")
          .xml(new XML()
            .name("address"));

        Schema propertyDefinition1 = new StringSchema();
        propertyDefinition1.setName("street");
        propertyDefinition1.setExample("12345 El Monte Blvd");

        Schema propertyDefinition2 = new StringSchema();
        propertyDefinition2.setName("city");
        propertyDefinition2.setExample("Los Altos Hills");

        Schema propertyDefinition3 = new StringSchema();
        propertyDefinition3.setName("state");
        propertyDefinition3.setExample("CA");
        propertyDefinition3.setMinLength(2);
        propertyDefinition3.setMaxLength(2);

        Schema propertyDefinition4 = new StringSchema();
        propertyDefinition4.setName("zip");
        propertyDefinition4.setExample("94022");

        address.addProperties("street",propertyDefinition1);
        address.addProperties("city",propertyDefinition2);
        address.addProperties("state",propertyDefinition3);
        address.addProperties("zip",propertyDefinition4);


        definitions.put("Address", address);

        // register the JSON serializer
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonNodeExampleSerializer());
        simpleModule.addDeserializer(Example.class, new JsonExampleDeserializer());
        Json.mapper().registerModule(simpleModule);

        Example rep = (Example) ExampleBuilder.fromSchema(new StringSchema().addEnumItem("hello").example("fun"), definitions, false);
        assertEqualsIgnoreLineEnding(Json.pretty(rep), "\"fun\"");
    }

    @Test
    public void testXmlExample() throws Exception {
        Schema model = new Schema();
        Schema property = new StringSchema()
                .name("id")
                .xml(new XML()
                        .name("fred"));
        model.addProperties("id", property);

        Map<String, Schema> definitions = new HashMap<>();
        definitions.put("User", model);
        assertEqualsIgnoreLineEnding(Json.pretty(ExampleBuilder.fromSchema(new Schema().$ref("User"), definitions, false)), "{\n  \"id\" : \"string\"\n}");
    }

    @Test
    public void testXmlBoolean() throws Exception {
        BooleanSchema sp = new BooleanSchema();
        Example ex = ExampleBuilder.fromSchema(sp, null, false);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><boolean>true</boolean>");
    }

    @Test
    public void testXmlDecimal() throws Exception {
        NumberSchema sp = new NumberSchema();
        Example ex = ExampleBuilder.fromSchema(sp, null, false);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><decimal>1.5</decimal>");
    }

    @Test
    public void testXmlFloat() throws Exception {
        NumberSchema sp = new NumberSchema();
        Example ex = ExampleBuilder.fromSchema(sp.format("float"), null,false);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><float>1.1</float>");
    }

    @Test
    public void testXmlInteger() throws Exception {
        IntegerSchema sp = new IntegerSchema();
        Example ex = ExampleBuilder.fromSchema(sp, null,false);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><integer>0</integer>");
    }

    @Test
    public void testXmlLong() throws Exception {
        IntegerSchema sp = new IntegerSchema();
        Example ex = ExampleBuilder.fromSchema(sp.format("int64"), null, false);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><long>0</long>");
    }

    @Test
    public void testXmlString() throws Exception {
        StringSchema sp = new StringSchema();
        Example ex = ExampleBuilder.fromSchema(sp, null, false);
        String xmlString = new XmlExampleSerializer().serialize(ex);
        assertEqualsIgnoreLineEnding(xmlString, "<?xml version='1.1' encoding='UTF-8'?><string>string</string>");
    }

    @Test
    public void testRecursiveModel() throws Exception {
        Schema person = new Schema();
        Schema property1 = new IntegerSchema().name("age").example(42);
        Schema property2 = new Schema().$ref("Person").name("spouse");

        person.addProperties("age", property1);
        person.addProperties("spouse", property2);

        Map<String, Schema> definitions = new HashMap<>();
        definitions.put("Person", person);

        Example rep = ExampleBuilder.fromSchema(new Schema().$ref("Person"), definitions, false);
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
        Schema model = Json.mapper().readValue(schema, Schema.class);

        Map<String, Schema> definitions = new HashMap<>();
        definitions.put("SimpleModel", model);

        Example rep = ExampleBuilder.fromSchema(new Schema().$ref("SimpleModel"), definitions, false);

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
        Schema model = Json.mapper().readValue(schema, Schema.class);

        Map<String, Schema> definitions = new HashMap<>();
        definitions.put("ComposedModel", model);

        Example rep = ExampleBuilder.fromSchema(new Schema().$ref("ComposedModel").name("ComposedModel"), definitions, false);

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
        Schema model = Json.mapper().readValue(schema, Schema.class);

        Map<String, Schema> definitions = new HashMap<>();
        definitions.put("Circular", model);

        Example rep = ExampleBuilder.fromSchema(new Schema().$ref("Circular"), definitions, false);

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
        Schema model = Json.mapper().readValue(schema, Schema.class);

        Map<String, Schema> definitions = new HashMap<>();
        definitions.put("InlineModel", model);

        Example rep = ExampleBuilder.fromSchema(new Schema().$ref("InlineModel"), definitions, false);

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
        IntegerSchema integerSchema = new IntegerSchema();
        integerSchema.setFormat("int64");
        integerSchema.setExample(new Long(4321));
        Schema model = new Schema();
        model.addProperties("int64",integerSchema);
        Map<String, Schema> definitions = new HashMap<>();
        definitions.put("Address", model);

        Example rep = ExampleBuilder.fromSchema(new Schema().$ref("Address"), definitions, false);
        assertEqualsIgnoreLineEnding(Json.pretty(rep),
                "{\n" +
                "  \"int64\" : 4321\n" +
                "}");
    }

    @Test
    public void testIssue127() throws Exception {
        IntegerSchema integerProperty = new IntegerSchema();
        integerProperty.setFormat(null);
        integerProperty.setExample(new Long(4321));
        Schema model = new Schema();
        model.addProperties("unboundedInteger",integerProperty);


        Map<String, Schema> definitions = new HashMap<>();
        definitions.put("Address", model);

        Example rep = ExampleBuilder.fromSchema(new Schema().$ref("Address"), definitions, false);

        Json.prettyPrint(rep);
        assertEqualsIgnoreLineEnding(Json.pretty(rep),
                "{\n" +
                "  \"unboundedInteger\" : 4321\n" +
                "}");
    }

    @Test
    public void testInvalidExample() throws Exception {
        testInvalidExample( new IntegerSchema(), "asd",
            ExampleBuilder.SAMPLE_INT_PROPERTY_VALUE, 123 );

        testInvalidExample( new IntegerSchema().format("int64"), "asd",
            ExampleBuilder.SAMPLE_LONG_PROPERTY_VALUE, 123 );

        testInvalidExample( new NumberSchema().format("float"), "asd",
            ExampleBuilder.SAMPLE_FLOAT_PROPERTY_VALUE, 2.1f );

        testInvalidExample( new NumberSchema().format("double"), "asd",
            ExampleBuilder.SAMPLE_DOUBLE_PROPERTY_VALUE, 3.1f );

        // base types that don't implement setting a sample value
        testInvalidExample( new NumberSchema(), "asd",
            ExampleBuilder.SAMPLE_DECIMAL_PROPERTY_VALUE );

        testInvalidExample( new IntegerSchema(), "asd",
            ExampleBuilder.SAMPLE_BASE_INTEGER_PROPERTY_VALUE );
    }

    public void testInvalidExample(Schema property, String invalidValue, Object defaultValue ) throws Exception {
       testInvalidExample( property, invalidValue, defaultValue, null );
    }

    public void testInvalidExample(Schema property, String invalidValue, Object defaultValue, Object sampleValue ) throws Exception {
        property.setExample( invalidValue);

        Schema model = new Schema();
        model.addProperties("test", property);


        Map<String, Schema> definitions = new HashMap<>();
        definitions.put("Test", model);

        // validate that the internal default value is returned if an invalid value is set
        ObjectExample rep = (ObjectExample) ExampleBuilder.fromSchema(new Schema().$ref("Test"), definitions, false);
        AbstractExample example = (AbstractExample) rep.get( "test" );
        System.out.println(example);
        assertEquals( example.asString(), String.valueOf(defaultValue) );

        // validate that a specified default value is returned if an invalid value is set
        if( sampleValue != null ) {
            property.setDefault(String.valueOf(sampleValue));
            rep = (ObjectExample) ExampleBuilder.fromSchema(new Schema().$ref("Test"), definitions, false);
            example = (AbstractExample) rep.get("test");
            assertEquals(String.valueOf(sampleValue), example.asString());
        }
    }

    private void assertEqualsIgnoreLineEnding(String actual, String expected) {
        assertEquals(actual.replace("\r\n", "\n"), expected);
    }

    @Test
    public void testObjectsWithAnonymousObjectArrays() throws Exception {

        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/issue-171.yaml");

        ApiResponse response = openAPI.getPaths().get("/test").getGet().getResponses().get( "200" );
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), openAPI.getComponents().getSchemas(), false);

        String output = Json.pretty(example);

        assertEqualsIgnoreLineEnding(output, "[ {\n" +
                "  \"id\" : \"string\",\n" +
                "  \"nestedArray\" : [ {\n" +
                "    \"id\" : \"string\",\n" +
                "    \"name\" : \"string\"\n" +
                "  } ]\n" +
                "} ]");


        response = openAPI.getPaths().get("/anothertest").getGet().getResponses().get( "200" );
        example = ExampleBuilder.fromSchema(response.getContent().get("*/*").getSchema(), openAPI.getComponents().getSchemas(), false);

        output = new XmlExampleSerializer().serialize(example);
        assertEquals( output, "<?xml version='1.1' encoding='UTF-8'?><string>string</string>");
    }

    @Test
    public void testEnumExample() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/issue-171.yaml");

        ApiResponse response = openAPI.getPaths().get("/color").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), openAPI.getComponents().getSchemas(), false);

        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"color\" : \"black\"\n" +
                "}");
    }

    @Test
    public void testIssue1261InlineSchemaExample() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/issue-1261.yaml");

        ApiResponse response = openAPI.getPaths().get("/user").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, false);

        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"id\" : 42,\n" +
                "  \"name\" : \"Arthur Dent\"\n" +
                "}");
    }

    @Test
    public void testIssue1177RefArrayExample() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/issue-1177.yaml");

        ApiResponse response = openAPI.getPaths().get("/array").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), openAPI.getComponents().getSchemas(), false);

        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "[ \"string\" ]");
    }

    @Test
    public void testInlinedArrayExample() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/array-example.yaml");

        ApiResponse response = openAPI.getPaths().get("/").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), openAPI.getComponents().getSchemas(), false);

        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "[ {\n" +
                "  \"id\" : 1,\n" +
                "  \"name\" : \"Arthur Dent\"\n" +
                "}, {\n" +
                "  \"id\" : 2,\n" +
                "  \"name\" : \"Ford Prefect\"\n" +
                "} ]");
    }

    @Test
    public void testNextedArrayExample() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/nested-array-example.yaml");

        ApiResponse response = openAPI.getPaths().get("/").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), openAPI.getComponents().getSchemas(), false);

        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "[ [ {\n" +
                "  \"id\" : 1,\n" +
                "  \"name\" : \"Arthur Dent\"\n" +
                "}, {\n" +
                "  \"id\" : 2,\n" +
                "  \"name\" : \"Ford Prefect\"\n" +
                "} ] ]");
    }

    @Test
    public void testIssue1263SchemaExampleNestedObjects() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/issue-1263.yaml");

        ApiResponse response = openAPI.getPaths().get("/nested_object").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), openAPI.getComponents().getSchemas(), false);

        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"nested_object\" : {\n" +
                "    \"foo\" : \"bar\"\n" +
                "  }\n" +
                "}");
    }

    @Test
    public void testDifferentExampleTypes() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/example-types.yaml");

        ApiResponse response = openAPI.getPaths().get("/user").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, false);

        String output = Json.pretty(example);
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
    public void writeOnlyParametersShouldNotBeIncluded() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/write-only.yaml");

        ApiResponse response = openAPI.getPaths().get("/user").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, false);

        String output = Json.pretty(example);
        // Password shouldn't be included
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"username\" : \"bob\"\n" +
                "}");
    }

    @Test
    public void resolveComposedOneOfSchema(){

        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/oneOf-anyOf.yaml");

        ApiResponse response = openAPI.getPaths().get("/mixed-array").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(),null,ExampleBuilder.RequestType.READ, false);
        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "[ \"string\" ]");

    }

    @Test
    public void verifyAdditionalPropertyResponse() throws Exception {

        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/oas3.yaml");
        ApiResponse response = openAPI.getPaths().get("/mockResponses/additionalPropertiesTest").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(),null,ExampleBuilder.RequestType.READ, false);
        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"foo\" : 0,\n" +
                "  \"additionalProp1\" : \"string\",\n" +
                "  \"additionalProp2\" : \"string\",\n" +
                "  \"additionalProp3\" : \"string\"\n" +
                "}");

    }

    @Test
    public void verifyAdditionalPropertiesWithArray() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/oas3_array.yaml");
        ApiResponse response = openAPI.getPaths().get("/dictionaryOfArray").getGet().getResponses().get("200");

        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), openAPI.getComponents().getSchemas(), ExampleBuilder.RequestType.READ, false);
        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"additionalProp1\" : [ {\n" +
                "    \"joel\" : \"string\",\n" +
                "    \"prop2\" : 0\n" +
                "  } ],\n" +
                "  \"additionalProp2\" : [ {\n" +
                "    \"joel\" : \"string\",\n" +
                "    \"prop2\" : 0\n" +
                "  } ],\n" +
                "  \"additionalProp3\" : [ {\n" +
                "    \"joel\" : \"string\",\n" +
                "    \"prop2\" : 0\n" +
                "  } ]\n" +
                "}");
    }

    @Test
    public void verifyAdditionalPropertiesWithPasswords() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/oas3_password.yaml");
        ApiResponse response = openAPI.getPaths().get("/dictionaryOfPassword").getGet().getResponses().get("200");

        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, false);
        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"additionalProp1\" : \"string\",\n" +
                "  \"additionalProp2\" : \"string\",\n" +
                "  \"additionalProp3\" : \"string\"\n" +
                "}");
    }

    @Test
    public void verifyGetMapResponse() throws Exception {

        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/oas3.yaml");
        ApiResponse response = openAPI.getPaths().get("/mockResponses/primitiveMapResponse").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(),null,ExampleBuilder.RequestType.READ, false);
        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"additionalProp1\" : \"string\",\n" +
                "  \"additionalProp2\" : \"string\",\n" +
                "  \"additionalProp3\" : \"string\"\n" +
                "}");
    }

    @Test
    public void verifyPropertyWithBooleanAdditionalProperty() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/oas3.yaml");
        ApiResponse response = openAPI.getPaths().get("/mockResponses/booleanAdditionalProperties").getGet().getResponses().get("200");

        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, false);
        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"firstProperty\" : \"string\"\n" +
                "}");
    }

    @Test
    public void verifyBooleanAdditionalPropertyTrue() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/oas3.yaml");
        ApiResponse response = openAPI.getPaths().get("/mockResponses/booleanAdditionalPropertiesTrue").getGet().getResponses().get("200");

        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, false);
        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{ }");
    }

    @Test
    public void verifyBooleanAdditionalPropertyFalse() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/oas3.yaml");
        ApiResponse response = openAPI.getPaths().get("/mockResponses/booleanAdditionalPropertiesFalse").getGet().getResponses().get("200");

        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, false);
        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{ }");
    }

    @Test
    public void resolveComposedOneOfRefSchema(@Injectable List<AuthorizationValue> auth){

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);

        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/oneOf-anyOf.yaml", auth, options);

        ApiResponse response = openAPI.getPaths().get("/oneOf").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(),null,ExampleBuilder.RequestType.READ, false);
        String output = Json.pretty(example);
        assertEqualsIgnoreLineEnding(output, "{\n" +
                "  \"title\" : \"The Hitchhiker's Guide to the Galaxy\",\n" +
                "  \"authors\" : [ \"Douglas Adams\" ],\n" +
                "  \"isbn\" : \"0-330-25864-8\"\n" +
                "}");

        ApiResponse responseAnyOf = openAPI.getPaths().get("/anyOf").getGet().getResponses().get("200");
        Example exampleAnyOf = ExampleBuilder.fromSchema(responseAnyOf.getContent().get("application/json").getSchema(),null,ExampleBuilder.RequestType.READ, false);
        String outputAnyOf = Json.pretty(exampleAnyOf);
        assertEqualsIgnoreLineEnding(outputAnyOf, "{\n" +
                "  \"title\" : \"Blade Runner\",\n" +
                "  \"directors\" : [ \"Ridley Scott\" ],\n" +
                "  \"year\" : 1982\n" +
                "}");

    }

    @Test
    public void testAdjacentComposedSchema(@Injectable List<AuthorizationValue> auth){

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);

        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/oneOf-anyOf.yaml", auth, options);


        ApiResponse responseAdjacent = openAPI.getPaths().get("/adjacent").getGet().getResponses().get("200");
        Example exampleAdjacent = ExampleBuilder.fromSchema(responseAdjacent.getContent().get("application/json").getSchema(),null,ExampleBuilder.RequestType.READ, false);
        String outputAdjacent = Json.pretty(exampleAdjacent);
        assertEqualsIgnoreLineEnding(outputAdjacent, "[ {\n" +
                "  \"title\" : \"The Hitchhiker's Guide to the Galaxy\",\n" +
                "  \"authors\" : [ \"Douglas Adams\" ],\n" +
                "  \"isbn\" : \"0-330-25864-8\"\n" +
                "}, {\n" +
                "  \"title\" : \"Blade Runner\",\n" +
                "  \"directors\" : [ \"Ridley Scott\" ],\n" +
                "  \"year\" : 1982\n" +
                "} ]");

    }

    @Test
    public void testRefAndInlineAllOf(@Injectable final List<AuthorizationValue> auths) throws Exception {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/allOfAndRef.yaml",auths,options);

        Assert.assertNotNull(openAPI);
        Assert.assertTrue(openAPI.getComponents().getSchemas().size() == 2);
        Assert.assertNotNull(openAPI.getComponents().getSchemas().get("UserEx"));
        Assert.assertNotNull(openAPI.getComponents().getSchemas().get("User"));
        Assert.assertTrue(openAPI.getPaths().get("/refToAllOf").getGet().getResponses().get("200").getContent().get("application/json").getSchema().getProperties().size() == 2);
    }

    @Test(description = "JSON example with xml name on array item")
    public void testIssue300() throws Exception {
        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/issue-300.yaml");
        Schema schema = openAPI.getComponents().getSchemas().get("Pet");
        Example example = ExampleBuilder.fromSchema(schema, openAPI.getComponents().getSchemas(),false);
        String jsonExample = Json.pretty(example);
        assertEqualsIgnoreLineEnding(jsonExample, "{\n  \"name\" : \"doggie\",\n  \"shots\" : [ \"rabies\" ]\n}");
        
        String xmlExample = new XmlExampleSerializer().serialize(example);
        assertEquals(xmlExample, "<?xml version='1.1' encoding='UTF-8'?><Pet><name>doggie</name><shots><shot>rabies</shot></shots></Pet>");
    }

    //@Test
    public void testSwos126() throws Exception {

        String spec = "openapi: 3.0.0\n" +
                "info:\n" +
                "  title: ExampleBuilder and date-time examples\n" +
                "  version: 0.0.0\n" +
                "paths: {}\n" +
                "\n" +
                "components:\n" +
                "  schemas:\n" +
                "    MyModel:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        date:\n" +
                "          type: string\n" +
                "          format: date\n" +
                "          example: '2019-08-05'\n" +
                "        dateTime:\n" +
                "          type: string\n" +
                "          format: date-time\n" +
                "          example: '2019-08-05T12:34:56Z'";
        String schemaName = "MyModel";
        // Load OAS3 definition
        OpenAPI openapi = new OpenAPIV3Parser().readContents(spec).getOpenAPI();

        // Create an Example object for the MyModel model
        Map<String, Schema> allSchemas = openapi.getComponents().getSchemas();
        Schema schema = allSchemas.get(schemaName);
        Example example = ExampleBuilder.fromSchema(schema, allSchemas, ExampleBuilder.RequestType.READ, false);

        // Configure JSON example serializer
        SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
        Json.mapper().registerModule(simpleModule);

        // Convert the Example object to a JSON string
        String jsonExample = Json.pretty(example);
        assertTrue(jsonExample.contains("\"date\" : \"2019-08-05\""));
        assertTrue(jsonExample.contains("\"dateTime\" : \"2019-08-05T12:34:56Z\""));
    }

    @Test
    public void testNullExampleSupportOAS3(){

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);

        OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/swagger/null-examples-oas3.yaml", null, options);

        /*ApiResponse response = openAPI.getPaths().get("/object-with-null-example").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, true);
        String output = Json.pretty(example);
        assertEquals(output, "{\n" +
                "  \"foo\" : null\n" +
                "}");

        response = openAPI.getPaths().get("/object-with-null-in-schema-example").getGet().getResponses().get("200");
        example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, true);
        output = Json.pretty(example);
        assertEquals(output, "{\n" +
                "  \"a\" : 5,\n" +
                "  \"b\" : \"test\",\n" +
                "  \"c\" : true,\n" +
                "  \"d\" : " + null + "\n" +
                "}");

        response = openAPI.getPaths().get("/object-with-null-property-example").getGet().getResponses().get("200");
        example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, true);
        output = Json.pretty(example);
        assertEquals(output, "{\n" +
                "  \"a\" : 5,\n" +
                "  \"b\" : " + null + "\n" +
                "}");
*/
        ApiResponse response = openAPI.getPaths().get("/string-with-null-example").getGet().getResponses().get("200");
        Example example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, true);
        String output = Json.pretty(example);
        Yaml.prettyPrint(output);
        assertNull(output);

        response = openAPI.getPaths().get("/array-with-null-array-example").getGet().getResponses().get("200");
        example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, true);
        output = Json.pretty(example);
        assertNull(output);

        response = openAPI.getPaths().get("/array-with-null-item-example").getGet().getResponses().get("200");
        example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, true);
        output = Json.pretty(example);
        assertEquals(output, "[" + null + "]");

        response = openAPI.getPaths().get("/array-with-null-in-array-example").getGet().getResponses().get("200");
        example = ExampleBuilder.fromSchema(response.getContent().get("application/json").getSchema(), null, ExampleBuilder.RequestType.READ, true);
        output = Json.pretty(example);
        assertEquals(output, "[\"foo\", " + null + "]");
    }
}
