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

import io.swagger.inflector.converters.ConversionException;
import io.swagger.inflector.converters.InputConverter;
import io.swagger.inflector.validators.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public class DefaultValidatorTest {
    InputConverter converter;
  
    @BeforeClass
    public void setup() {
        converter = InputConverter.getInstance()
            .defaultConverters()
            .defaultValidators();
    }
    
    @Test
    public void testOptionalParameter() throws Exception {
        Parameter parameter = new QueryParameter()
            .name("test")
            .property(new StringProperty());

        converter.validate(null, parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testRequiredParameter() throws Exception {
        Parameter parameter = new QueryParameter()
            .name("test")
            .required(true)
            .property(new StringProperty());

        converter.validate(null, parameter);
    }

    @Test(expectedExceptions = ConversionException.class)
    public void testInvalidDatatype() throws Exception {
        Parameter parameter = new QueryParameter()
            .name("test")
            .required(true)
            .property(new IntegerProperty());

        converter.convertAndValidate(Arrays.asList("oops"), parameter, Integer.class, null);
    }
}