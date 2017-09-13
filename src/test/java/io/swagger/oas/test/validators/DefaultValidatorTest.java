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

import io.swagger.oas.inflector.converters.ConversionException;
import io.swagger.oas.inflector.converters.InputConverter;
import io.swagger.oas.inflector.validators.*;
import io.swagger.oas.models.media.IntegerSchema;
import io.swagger.oas.models.media.StringSchema;
import io.swagger.oas.models.parameters.Parameter;


import io.swagger.oas.models.parameters.QueryParameter;
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
            .schema(new StringSchema());

        converter.validate(null, parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testRequiredParameter() throws Exception {
        Parameter parameter = new QueryParameter()
            .name("test")
            .required(true)
            .schema(new StringSchema());

        converter.validate(null, parameter);
    }

    @Test(expectedExceptions = ConversionException.class)
    public void testInvalidDatatype() throws Exception {
        Parameter parameter = new QueryParameter()
            .name("test")
            .required(true)
            .schema(new IntegerSchema());

        converter.convertAndValidate(Arrays.asList("oops"), parameter, Integer.class, null);
    }
}