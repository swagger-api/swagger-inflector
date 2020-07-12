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

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.models.IntegerExample;
import io.swagger.oas.inflector.examples.models.ObjectExample;
import io.swagger.oas.inflector.examples.models.StringExample;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.oas.test.models.User;
import io.swagger.v3.core.util.Json;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ResponseModelTest {
    @Test
    public void testConvertStringProperty() throws Exception {
        StringSchema p = new StringSchema();

        Object o = ExampleBuilder.fromSchema(p, null, false);
        assertNotNull(o);
        assertTrue(o instanceof StringExample);
        assertEquals(((StringExample) o).textValue(), "string");
    }

    @Test
    public void testConvertStringPropertyWithExample() throws Exception {
        Schema p = new StringSchema()
                .example("fun");

        Object o = ExampleBuilder.fromSchema(p, null, false);
        assertNotNull(o);
        assertTrue(o instanceof StringExample);
        assertEquals(((StringExample) o).textValue(), "fun");
    }

    @Test
    public void testConvertIntegerProperty() throws Exception {
        IntegerSchema p = new IntegerSchema();

        Object o = ExampleBuilder.fromSchema(p, null, false);
        assertNotNull(o);
        assertTrue(o instanceof IntegerExample);
        assertEquals(((IntegerExample) o).asInt(), new Integer(0));
    }

    @Test
    public void testConvertIntegerPropertyWithExample() throws Exception {
        Schema p = new IntegerSchema()
                .example(3);

        Object o = ExampleBuilder.fromSchema(p, null, false);
        assertNotNull(o);
        assertTrue(o instanceof IntegerExample);
        assertEquals(((IntegerExample) o).asInt(), new Integer(3));
    }

    @Test
    public void testComplexModel() throws Exception {
        Schema property = new Schema().$ref("User");
        Map<String, Schema> definitions = ModelConverters.getInstance().readAll(User.class);
        Object o = ExampleBuilder.fromSchema(property, definitions, false);

        ObjectExample n = Json.mapper().convertValue(o, ObjectExample.class);
        assertNotNull(n);
    }
}