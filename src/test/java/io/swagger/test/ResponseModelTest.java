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

package io.swagger.test;

import io.swagger.converter.ModelConverters;
import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.models.IntegerExample;
import io.swagger.inflector.examples.models.ObjectExample;
import io.swagger.inflector.examples.models.StringExample;
import io.swagger.models.Model;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.test.models.User;
import io.swagger.util.Json;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ResponseModelTest {
    @Test
    public void testConvertStringProperty() throws Exception {
        StringProperty p = new StringProperty();

        Object o = ExampleBuilder.fromProperty(p, null);
        assertNotNull(o);
        assertTrue(o instanceof StringExample);
        assertEquals(((StringExample) o).textValue(), "string");
    }

    @Test
    public void testConvertStringPropertyWithExample() throws Exception {
        StringProperty p = new StringProperty()
                .example("fun");

        Object o = ExampleBuilder.fromProperty(p, null);
        assertNotNull(o);
        assertTrue(o instanceof StringExample);
        assertEquals(((StringExample) o).textValue(), "fun");
    }

    @Test
    public void testConvertIntegerProperty() throws Exception {
        IntegerProperty p = new IntegerProperty();

        Object o = ExampleBuilder.fromProperty(p, null);
        assertNotNull(o);
        assertTrue(o instanceof IntegerExample);
        assertEquals(((IntegerExample) o).asInt(), new Integer(0));
    }

    @org.junit.Test
    public void testConvertIntegerPropertyWithExample() throws Exception {
        IntegerProperty p = new IntegerProperty()
                .example(3);

        Object o = ExampleBuilder.fromProperty(p, null);
        assertNotNull(o);
        assertTrue(o instanceof IntegerExample);
        assertEquals(((IntegerExample) o).asInt(), new Integer(3));
    }

    @Test
    public void testComplexModel() throws Exception {
        RefProperty p = new RefProperty("User");
        Map<String, Model> definitions = ModelConverters.getInstance().readAll(User.class);
        Object o = ExampleBuilder.fromProperty(p, definitions);

        ObjectExample n = Json.mapper().convertValue(o, ObjectExample.class);
        assertNotNull(n);
    }
}