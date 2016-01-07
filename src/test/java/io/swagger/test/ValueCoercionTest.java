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

import io.swagger.inflector.converters.ConversionException;
import io.swagger.inflector.converters.DefaultConverter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.EmailProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.UUIDProperty;
import io.swagger.util.Json;

import org.testng.annotations.Test;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ValueCoercionTest {
    DefaultConverter utils = new DefaultConverter();
    TypeFactory tf = Json.mapper().getTypeFactory();

    @Test
    public void testConvertStringValue() throws Exception {
        List<String> values = Arrays.asList("a");

        Parameter parameter = new QueryParameter().items(new StringProperty());
        Object o = utils.cast(values, parameter, tf.constructType(String.class), null);

        assertTrue(o instanceof String);
    }

    @Test
    public void testConvertIntegerValue() throws Exception {
        List<String> values = Arrays.asList("1");

        Parameter parameter = new QueryParameter().items(new IntegerProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Integer.class), null);

        assertTrue(o instanceof Integer);
    }

    @Test(expectedExceptions = ConversionException.class)
    public void testConvertInvalidIntegerValue() throws Exception {
        List<String> values = Arrays.asList("1abczdf");

        Parameter parameter = new QueryParameter().items(new IntegerProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Integer.class), null);

        assertNull(o);
    }

    @Test
    public void testConvertLongValue() throws Exception {
        List<String> values = Arrays.asList("1");

        Parameter parameter = new QueryParameter().items(new LongProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Long.class), null);

        assertTrue(o instanceof Long);
    }

    @Test(expectedExceptions = ConversionException.class)
    public void testConvertInvalidLongValue() throws Exception {
        List<String> values = Arrays.asList("1zzzzz");

        Parameter parameter = new QueryParameter().items(new LongProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Long.class), null);

        assertNull(o);
    }

    @Test
    public void testConvertFloatValue() throws Exception {
        List<String> values = Arrays.asList("1");

        Parameter parameter = new QueryParameter().items(new FloatProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Float.class), null);

        assertTrue(o instanceof Float);
    }

    @Test(expectedExceptions = ConversionException.class)
    public void testConvertInvalidFloatValue() throws Exception {
        List<String> values = Arrays.asList("1;;lkaj;lasjkdfs");

        Parameter parameter = new QueryParameter().items(new FloatProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Float.class), null);

        assertNull(o);
    }

    @Test
    public void testConvertDoubleValue() throws Exception {
        List<String> values = Arrays.asList("1");

        Parameter parameter = new QueryParameter().items(new DoubleProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Double.class), null);

        assertTrue(o instanceof Double);
    }

    @Test(expectedExceptions = ConversionException.class)
    public void testConvertInvalidDoubleValue() throws Exception {
        List<String> values = Arrays.asList("abcdefg");

        Parameter parameter = new QueryParameter().items(new DoubleProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Double.class), null);

        assertNull(o);
    }

    @Test
    public void testConvertBooleanValue() throws Exception {
        List<String> values = Arrays.asList("true");

        Parameter parameter = new QueryParameter().items(new BooleanProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Boolean.class), null);

        assertTrue(o instanceof Boolean);
    }

    @Test
    public void testConvertBooleanValue1() throws Exception {
        List<String> values = Arrays.asList("1");

        Parameter parameter = new QueryParameter().items(new BooleanProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Boolean.class), null);

        assertTrue(o instanceof Boolean);
        assertTrue((Boolean) o);
    }

    @Test
    public void testConvertBooleanValue2() throws Exception {
        List<String> values = Arrays.asList("0");

        Parameter parameter = new QueryParameter().items(new BooleanProperty());
        Object o = utils.cast(values, parameter, tf.constructType(Boolean.class), null);

        assertTrue(o instanceof Boolean);
        assertFalse((Boolean) o);
    }

    @Test
    public void testConvertUUIDValue() throws Exception {
        List<String> values = Arrays.asList("163e1000-2a5a-4be2-b271-3470b63dff00");

        Parameter parameter = new QueryParameter().items(new UUIDProperty());
        Object o = utils.cast(values, parameter, tf.constructType(UUID.class), null);

        assertTrue(o instanceof UUID);
    }

    @Test(expectedExceptions = ConversionException.class)
    public void testConvertInvalidUUIDValue() throws Exception {
        List<String> values = Arrays.asList("bleh");

        Parameter parameter = new QueryParameter().items(new UUIDProperty());
        Object o = utils.cast(values, parameter, tf.constructType(UUID.class), null);

        assertNull(o);
    }

    @Test
    public void testConvertEmailValue() throws Exception {
        List<String> values = Arrays.asList("fehguy@gmail.com");

        Parameter parameter = new QueryParameter().items(new EmailProperty());
        Object o = utils.cast(values, parameter, tf.constructType(String.class), null);

        assertTrue(o instanceof String);
    }

    @Test
    public void testConvertDateValue() throws Exception {
        List<String> values = Arrays.asList("2005-12-31");

        Parameter parameter = new QueryParameter().items(new DateProperty());
        Object o = utils.cast(values, parameter, tf.constructType(LocalDate.class), null);

        assertEquals(o.toString(), "2005-12-31");
        assertTrue(o instanceof LocalDate);
    }

    @Test(expectedExceptions = ConversionException.class)
    public void testConvertInvalidDateValue() throws Exception {
        List<String> values = Arrays.asList("Booyah!");

        Parameter parameter = new QueryParameter().items(new DateProperty());
        Object o = utils.cast(values, parameter, tf.constructType(LocalDate.class), null);

        assertNull(o);
    }

    @Test
    public void testConvertDateTimeValue() throws Exception {
        List<String> values = Arrays.asList("2005-12-31T01:23:45.600-08:00");

        Parameter parameter = new QueryParameter().items(new DateTimeProperty());
        Object o = utils.cast(values, parameter, tf.constructType(DateTime.class), null);

        assertEquals(o.toString(), "2005-12-31T01:23:45.600-08:00");
        assertTrue(o instanceof DateTime);
    }

    @Test(expectedExceptions = ConversionException.class)
    public void testConvertInvalidDateTimeValue() throws Exception {
        List<String> values = Arrays.asList("Booyah!");

        Parameter parameter = new QueryParameter().items(new DateTimeProperty());
        Object o = utils.cast(values, parameter, tf.constructType(DateTime.class), null);

        assertNull(o);
    }
}
