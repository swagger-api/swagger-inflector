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

package io.swagger.oas.test.validators;

import io.swagger.oas.inflector.converters.InputConverter;
import io.swagger.oas.inflector.validators.ValidationException;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;

public class NumericValidatorTest {
    InputConverter converter;

    @BeforeClass
    public void setup() {
        converter = InputConverter.getInstance()
            .defaultConverters()
            .defaultValidators();
    }

    @Test
    public void testValidIntegerMinimum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMinimum(new BigDecimal("10.0"));
        parameter.setSchema(schema);

        converter.validate(new Integer(11), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerMinimum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMinimum(new BigDecimal("10.0"));
        parameter.setSchema(schema);

        converter.validate(new Integer(9), parameter);
    }

    @Test
    public void testEqualIntegerMinimum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMinimum(new BigDecimal("10.0"));
        parameter.setSchema(schema);

        converter.validate(new Integer(10), parameter);
    }

    @Test
    public void testValidIntegerMaximum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMaximum(new BigDecimal("10.0"));
        parameter.setSchema(schema);

        converter.validate(new Integer(9), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerMaximum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMaximum(new BigDecimal("10.0"));
        parameter.setSchema(schema);

        converter.validate(new Integer(11), parameter);
    }

    @Test
    public void testValidIntegerExclusiveMinimum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMinimum(new BigDecimal("10.0"));
        schema.setExclusiveMinimum(true);
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Integer(11), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerExclusiveMinimumEquality() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMinimum(new BigDecimal("10.0"));
        schema.setExclusiveMinimum(true);
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Integer(10), parameter);
    }

    @Test
    public void testValidIntegerExclusiveMaximum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMaximum(new BigDecimal("10.0"));
        schema.setExclusiveMaximum(true);
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Integer(9), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerExclusiveMaximum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMaximum(new BigDecimal("10"));
        schema.setExclusiveMaximum(true);
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Integer(11), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerExclusiveMaximumEquality() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMaximum(new BigDecimal("10.0"));
        schema.setExclusiveMaximum(true);
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Integer(10), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testIssue127_b() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setMaximum(new BigDecimal("10"));
        schema.setExclusiveMaximum(true);
        parameter.setSchema(schema);

        InputConverter.getInstance().validate("value 1", parameter);
    }

    @Test
    public void testValidIntegerEnum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setEnum(Arrays.asList("1", "2", "3"));
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Integer(3), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerEnum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new Schema();
        schema.setEnum(Arrays.asList("1", "2", "3"));
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Integer(4), parameter);
    }

    @Test
    public void testValidLongEnum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new NumberSchema();
        schema.setEnum(Arrays.asList("1", "2", "3"));
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Long(3), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidLongEnum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new NumberSchema();
        schema.setEnum(Arrays.asList("1", "2", "3"));
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Long(4), parameter);
    }

    @Test
    public void testValidDoubleEnum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new NumberSchema();
        schema.setEnum(Arrays.asList(
                new Double(1).toString(),
                new Double(2).toString(),
                new Double(3).toString()));
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Double(3), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidDoubleEnum() throws Exception {
        QueryParameter parameter = new QueryParameter();
        parameter.setName("test");
        Schema schema = new NumberSchema();
        schema.setEnum(Arrays.asList(
                new Double(1).toString(),
                new Double(2).toString(),
                new Double(3).toString()));
        parameter.setSchema(schema);

        InputConverter.getInstance().validate(new Double(4), parameter);
    }
}