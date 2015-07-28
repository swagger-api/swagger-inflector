/*
 *  Copyright 2015 SmartBear Software
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

import io.swagger.inflector.utils.ReflectionUtils;
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
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class SerializableParamExtractionTest {
    ReflectionUtils utils = new ReflectionUtils();

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
        Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new StringProperty()), null);
        assertEquals(cls, String.class);
    }

    @Test
    public void getIntegerParameterClassTest() throws Exception {
        Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new IntegerProperty()), null);
        assertEquals(cls, Integer.class);
    }

    @Test
    public void getLongParameterClassTest() throws Exception {
        Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new LongProperty()), null);
        assertEquals(cls, Long.class);
    }

    @Test
    public void getFloatParameterClassTest() throws Exception {
        Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new FloatProperty()), null);
        assertEquals(cls, Float.class);
    }

    @Test
    public void getDoubleParameterClassTest() throws Exception {
        Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new DoubleProperty()), null);
        assertEquals(cls, Double.class);
    }

    @Test
    public void getBooleanParameterClassTest() throws Exception {
        Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new BooleanProperty()), null);
        assertEquals(cls, Boolean.class);
    }

    @Test
    public void getDateParameterClassTest() throws Exception {
        Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new DateProperty()), null);
        assertEquals(cls, LocalDate.class);
    }

    @Test
    public void getDateTimeParameterClassTest() throws Exception {
        Class<?> cls = utils.getParameterSignature(new QueryParameter().property(new DateTimeProperty()), null);
        assertEquals(cls, DateTime.class);
    }

    @Test
    public void getStringArrayParameterClassTest() throws Exception {
        Class<?> cls = utils.getParameterSignature(new QueryParameter()
                .property(new ArrayProperty(new StringProperty())), null);
        assertEquals(cls, List.class);
    }
}