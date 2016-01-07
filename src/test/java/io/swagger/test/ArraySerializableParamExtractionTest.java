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

import io.swagger.inflector.converters.DefaultConverter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ArraySerializableParamExtractionTest {
    DefaultConverter utils = new DefaultConverter();
    TypeFactory tf = Json.mapper().getTypeFactory();

    @org.junit.Test
    public void testConvertStringArray() throws Exception {
        List<String> values = Arrays.asList("a", "b");

        Parameter parameter = new QueryParameter().items(new ArrayProperty().items(new StringProperty()));
        Object o = utils.cast(values, parameter, tf.constructArrayType(String.class), null);

        assertTrue(o instanceof List);

        @SuppressWarnings("unchecked")
        List<String> objs = (List<String>) o;
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

        Object o = utils.cast(values, parameter, tf.constructArrayType(String.class), null);

        assertTrue(o instanceof List);

        @SuppressWarnings("unchecked")
        List<String> objs = (List<String>) o;

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

        Object o = utils.cast(values, parameter, tf.constructArrayType(String.class), null);

        assertTrue(o instanceof List);

        @SuppressWarnings("unchecked")
        List<String> objs = (List<String>) o;

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

        Object o = utils.cast(values, parameter, tf.constructArrayType(String.class), null);

        assertTrue(o instanceof List);

        @SuppressWarnings("unchecked")
        List<String> objs = (List<String>) o;

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

        Object o = utils.cast(values, parameter, tf.constructArrayType(String.class), null);

        assertTrue(o instanceof List);

        @SuppressWarnings("unchecked")
        List<String> objs = (List<String>) o;

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

        Object o = utils.cast(values, parameter, tf.constructArrayType(Integer.class), null);

        assertTrue(o instanceof List);

        @SuppressWarnings("unchecked")
        List<Integer> objs = (List<Integer>) o;

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

        Object o = utils.cast(values, parameter, tf.constructArrayType(Boolean.class), null);

        assertTrue(o instanceof List);

        @SuppressWarnings("unchecked")
        List<Boolean> objs = (List<Boolean>) o;

        assertTrue(objs.size() == 3);
        assertEquals(objs.get(0), Boolean.TRUE);
        assertEquals(objs.get(1), Boolean.FALSE);
        assertEquals(objs.get(2), Boolean.TRUE);
    }
}
