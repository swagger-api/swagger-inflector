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

package io.swagger.oas.test;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.converter.ModelConverters;
import io.swagger.oas.inflector.config.Configuration;
import io.swagger.oas.inflector.utils.ReflectionUtils;


import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.media.StringSchema;
import io.swagger.oas.models.parameters.Parameter;

import io.swagger.oas.models.parameters.RequestBody;
import io.swagger.oas.test.models.Person;
import io.swagger.oas.test.models.User;
import org.junit.Before;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;

public class BodyParamExtractionTest {
    ReflectionUtils utils = new ReflectionUtils();

    @BeforeClass
    @Before
    public void setup() {
        Configuration config = new Configuration();
        config.setModelPackage("io.swagger.test.models");
        config.addModelMapping("User", User.class);

        utils.setConfiguration(config);
    }

    @Test
    public void testStringBodyParam() throws Exception {
        Map<String, Schema> definitions = new HashMap<String, Schema>();

        Parameter parameter = new Parameter().schema(new Schema().type("string"));
        JavaType jt = utils.getTypeFromParameter(parameter, definitions);

        assertEquals(jt.getRawClass(), String.class);
    }

    @org.junit.Test
    public void testUUIDBodyParam() throws Exception {
        Map<String, Schema> definitions = new HashMap<String, Schema>();

        Parameter parameter = new Parameter().schema(new Schema().type("string").format("uuid"));
        JavaType jt = utils.getTypeFromParameter(parameter, definitions);

        assertEquals(jt.getRawClass(), UUID.class);
    }

    @Test
    public void testConvertComplexBodyParamWithConfigMapping() throws Exception {
        Map<String, Schema> definitions = new HashMap<String, Schema>();

        Parameter parameter = new Parameter().schema(new Schema().$ref("#/definitions/User"));
        JavaType jt = utils.getTypeFromParameter(parameter, definitions);

        assertEquals(jt.getRawClass(), User.class);
    }

    @Test
    public void testConvertComplexBodyParamWithoutConfigMapping() throws Exception {
        Map<String, Schema> definitions = new HashMap<>();

        Parameter parameter = new Parameter().schema(new Schema().$ref("#/definitions/Person"));
        JavaType jt = utils.getTypeFromParameter(parameter, definitions);

        // will look up from the config model package and ref.simpleName of Person
        assertEquals(jt.getRawClass(), Person.class);
    }

    @Test
    public void testConvertComplexArrayBodyParam() throws Exception {
        Map<String, Schema> definitions = ModelConverters.getInstance().read(Person.class);

        Parameter parameter = new Parameter()
            .schema(new ArraySchema()
                .items(new Schema().$ref("#/definitions/Person")));
    
        JavaType jt = utils.getTypeFromParameter(parameter, definitions);
        assertEquals(jt.getRawClass(), Person[].class);
    }

    @Test
    public void testConvertPrimitiveArrayBodyParam() throws Exception {
        Map<String, Schema> definitions = ModelConverters.getInstance().read(Person.class);

        Parameter parameter = new Parameter()
            .schema(new ArraySchema()
                .items(new StringSchema()));

        JavaType jt = utils.getTypeFromParameter(parameter, definitions);
        assertEquals(jt.getRawClass(), String[].class);
    }

    @org.junit.Test
    public void testConvertDoubleArrayBodyParam() throws Exception {
        Map<String, Schema> definitions = ModelConverters.getInstance().read(Person.class);

        Parameter parameter = new Parameter()
            .schema(new ArraySchema()
                .items(new ArraySchema()));

        JavaType jt = utils.getTypeFromParameter(parameter, definitions);
        assertEquals(jt.getRawClass(), String[][].class);
    }
}
