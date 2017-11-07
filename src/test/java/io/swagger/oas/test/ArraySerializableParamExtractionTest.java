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

import io.swagger.oas.inflector.converters.DefaultConverter;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import io.swagger.v3.core.util.Json;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ArraySerializableParamExtractionTest {
    DefaultConverter utils = new DefaultConverter();
    TypeFactory tf = Json.mapper().getTypeFactory();

    @Test
    public void testConvertStringArray() throws Exception {
        List<String> values = Arrays.asList("a", "b");

        Parameter parameter = new QueryParameter().schema(new ArraySchema().items(new StringSchema()));
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
                .style(Parameter.StyleEnum.FORM)
                .explode(false)//("csv")
                .schema(new ArraySchema()
                        .items(new StringSchema()));

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
                .style(Parameter.StyleEnum.FORM)
                .explode(false)//("csv")
                .schema(new ArraySchema()
                        .items(new StringSchema()));

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
                .style(Parameter.StyleEnum.PIPEDELIMITED)
                .explode(false)//("csv")
                .schema(new ArraySchema()
                        .items(new StringSchema()));

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
                .style(Parameter.StyleEnum.SPACEDELIMITED)
                .explode(false)//("csv")
                .schema(new ArraySchema()
                        .items(new StringSchema()));

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
                .style(Parameter.StyleEnum.SPACEDELIMITED)
                .explode(false)//("csv")
                .schema(new ArraySchema()
                        .items(new IntegerSchema()));

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
                .style(Parameter.StyleEnum.SPACEDELIMITED)
                .schema(new ArraySchema()
                        .items(new BooleanSchema()));

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
