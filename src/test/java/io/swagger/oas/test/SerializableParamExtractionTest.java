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
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.media.BooleanSchema;
import io.swagger.oas.models.media.DateSchema;
import io.swagger.oas.models.media.DateTimeSchema;
import io.swagger.oas.models.media.IntegerSchema;
import io.swagger.oas.models.media.NumberSchema;
import io.swagger.oas.models.media.StringSchema;
import io.swagger.oas.models.parameters.QueryParameter;


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
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().schema(new StringSchema()), null);
        assertEquals(jt.getRawClass(), String.class);
    }



    @Test
    public void getIntegerParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().schema(new IntegerSchema()), null);
        assertEquals(jt.getRawClass(), Integer.class);
    }

    @Test
    public void getLongParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().schema(new IntegerSchema().format("int64")), null);
        assertEquals(jt.getRawClass(), Long.class);
    }


    @Test
    public void getFloatParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().schema(new NumberSchema().format("float")), null);
        assertEquals(jt.getRawClass(), Float.class);
    }

    @Test
    public void getDoubleParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().schema(new NumberSchema().format("double")), null);
        assertEquals(jt.getRawClass(), Double.class);
    }

    @Test
    public void getBooleanParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().schema(new BooleanSchema()), null);
        assertEquals(jt.getRawClass(), Boolean.class);
    }

    @Test
    public void getDateParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().schema(new DateSchema()), null);
        assertEquals(jt.getRawClass(), LocalDate.class);
    }

    @Test
    public void getDateTimeParameterClassTest() throws Exception {
        JavaType jt = utils.getTypeFromParameter(new QueryParameter().schema(new DateTimeSchema()), null);
        assertEquals(jt.getRawClass(), DateTime.class);
    }


}