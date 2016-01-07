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
import io.swagger.models.Operation;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.BaseIntegerProperty;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JavaType;

import static org.testng.Assert.assertEquals;

public class SerializableParamExtractionTest {
    DefaultConverter utils = new DefaultConverter();

    @Test
    public void getMethodGenerationNameTest() throws Exception {
        Operation operation = new Operation();
        String methodName = utils.getMethodName("/foo/bar", "GET", operation);

        assertEquals(methodName, "fooBarGET");
    }

    @Test
    public void getMethodNameWithOperationIDTest() throws Exception {
        Operation operation = new Operation().operationId("superFun");
        String methodName = utils.getMethodName("/foo/bar", "GET", operation);

        assertEquals(methodName, "superFun");
    }

    @Test
    public void getStringParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().property(new StringProperty()), null);
        assertEquals(jt.getRawClass(), String.class);
    }

    @Test
    public void getStringFallbackParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().property(new StringProperty("url")), null);
        assertEquals(jt.getRawClass(), String.class);
    }

    @Test
    public void getIntegerParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().property(new IntegerProperty()), null);
        assertEquals(jt.getRawClass(), Integer.class);
    }

    @Test
    public void getLongParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().property(new LongProperty()), null);
        assertEquals(jt.getRawClass(), Long.class);
    }

    @Test
    public void getLongFallbackParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().property(new BaseIntegerProperty("abc123")), null);
        assertEquals(jt.getRawClass(), Long.class);
    }

    @Test
    public void getFloatParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().property(new FloatProperty()), null);
        assertEquals(jt.getRawClass(), Float.class);
    }

    @Test
    public void getDoubleParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().property(new DoubleProperty()), null);
        assertEquals(jt.getRawClass(), Double.class);
    }

    @Test
    public void getBooleanParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().property(new BooleanProperty()), null);
        assertEquals(jt.getRawClass(), Boolean.class);
    }

    @Test
    public void getDateParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().property(new DateProperty()), null);
        assertEquals(jt.getRawClass(), LocalDate.class);
    }

    @Test
    public void getDateTimeParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().property(new DateTimeProperty()), null);
        assertEquals(jt.getRawClass(), DateTime.class);
    }

    @org.junit.Test
    public void getStringArrayParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter()
                .property(new ArrayProperty(new StringProperty())), null);
        assertEquals(jt.getRawClass(), String[].class);
    }
}