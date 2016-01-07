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

package io.swagger.test.validators;

import io.swagger.inflector.converters.InputConverter;
import io.swagger.inflector.validators.ValidationException;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.LongProperty;

import java.util.Arrays;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMinimum(10.0);

        converter.validate(new Integer(11), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerMinimum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMinimum(10.0);

        converter.validate(new Integer(9), parameter);
    }

    @Test
    public void testValidIntegerMaximum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMaximum(10.0);

        converter.validate(new Integer(9), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerMaximum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMaximum(10.0);

        converter.validate(new Integer(11), parameter);
    }

    @Test
    public void testValidIntegerExclusiveMinimum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMinimum(10.0);
        parameter.setExclusiveMinimum(true);

        InputConverter.getInstance().validate(new Integer(11), parameter);
    }

    @Test
    public void testValidIntegerExclusiveMinimumEquality() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMinimum(10.0);
        parameter.setExclusiveMinimum(true);

        InputConverter.getInstance().validate(new Integer(10), parameter);
    }

    @Test
    public void testValidIntegerExclusiveMaximum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMaximum(10.0);
        parameter.setExclusiveMaximum(true);

        InputConverter.getInstance().validate(new Integer(9), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerExclusiveMaximum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMaximum(10.0);
        parameter.setExclusiveMaximum(true);

        InputConverter.getInstance().validate(new Integer(11), parameter);
    }

    @Test
    public void testValidIntegerExclusiveMaximumEquality() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMaximum(10.0);
        parameter.setExclusiveMaximum(true);

        InputConverter.getInstance().validate(new Integer(10), parameter);
    }

    @Test
    public void testValidIntegerEnum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test")
            ._enum(Arrays.asList("1", "2", "3"));

        InputConverter.getInstance().validate(new Integer(3), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerEnum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test")
            ._enum(Arrays.asList("1", "2", "3"));

        InputConverter.getInstance().validate(new Integer(4), parameter);
    }

    @Test
    public void testValidLongEnum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test")
            ._enum(Arrays.asList("1", "2", "3"))
            .property(new LongProperty());

        InputConverter.getInstance().validate(new Long(3), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidLongEnum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test")
            ._enum(Arrays.asList("1", "2", "3"))
            .property(new LongProperty());

        InputConverter.getInstance().validate(new Long(4), parameter);
    }

    @Test
    public void testValidDoubleEnum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test")
            ._enum(Arrays.asList(
                new Double(1).toString(),
                new Double(2).toString(),
                new Double(3).toString()))
            .property(new DoubleProperty());

        InputConverter.getInstance().validate(new Double(3), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidDoubleEnum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test")
            ._enum(Arrays.asList(
                new Double(1).toString(),
                new Double(2).toString(),
                new Double(3).toString()))
            .property(new DoubleProperty());

        InputConverter.getInstance().validate(new Double(4), parameter);
    }
}