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

package io.swagger.test.validators;

import io.swagger.inflector.converters.ConversionException;
import io.swagger.inflector.converters.InputConverter;
import io.swagger.inflector.validators.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

public class NumericValidatorTest {
    @Test
    public void testValidIntegerMinimum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMinimum(10.0);

        InputConverter.getInstance().validate(new Integer(11), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerMinimum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMinimum(10.0);

        InputConverter.getInstance().validate(new Integer(9), parameter);
    }

    @Test
    public void testValidIntegerMaximum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMaximum(10.0);

        InputConverter.getInstance().validate(new Integer(9), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidIntegerMaximum() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test");
        parameter.setMaximum(10.0);

        InputConverter.getInstance().validate(new Integer(11), parameter);
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
}