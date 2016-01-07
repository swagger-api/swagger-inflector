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
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.StringProperty;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class StringTypeValidatorTest {
    InputConverter converter;
  
    @BeforeClass
    public void setup() {
        converter = InputConverter.getInstance()
            .defaultConverters()
            .defaultValidators();
    }

    @Test
    public void testDateTimeConversion() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test")
            .property(new DateTimeProperty());

        converter.validate(new DateTime(), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidDateTimeConversion() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test")
            .property(new DateTimeProperty());

        converter.validate(new Integer(3), parameter);
    }

    @Test
    public void testValidLocalDateConversion() throws Exception {
        QueryParameter parameter = new QueryParameter()
            .name("test")
            .property(new DateTimeProperty());

        converter.validate(new LocalDate(), parameter);
    }

    @Test
    public void testLocalDateConversionEnum() throws Exception {
        List<String> values = new ArrayList<String>();
        for(int i = 1; i <= 3; i++) {
            String str = "2015-01-0" + i;
            values.add(str);
        }

        QueryParameter parameter = new QueryParameter()
            .name("test")
            .property(new DateProperty());
        
        parameter._enum(values);

        converter.validate(new LocalDate("2015-01-02"), parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidLocalDateConversionEnum() throws Exception {
        List<String> values = new ArrayList<String>();
        for(int i = 1; i <= 3; i++) {
            String str = "2015-01-0" + i;
            values.add(str);
        }

        QueryParameter parameter = new QueryParameter()
            .name("test")
            .property(new DateProperty());
        
        parameter._enum(values);

        converter.validate(new LocalDate("2015-01-04"), parameter);
    }

    @Test
    public void testStringValueEnum() throws Exception {
        List<String> values = new ArrayList<String>();
        for(int i = 1; i <= 3; i++) {
            String str = "allowable_" + i;
            values.add(str);
        }

        QueryParameter parameter = new QueryParameter()
            .name("test")
            .property(new StringProperty());
        
        parameter._enum(values);

        converter.validate("allowable_1", parameter);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testInvalidStringValueEnum() throws Exception {
        List<String> values = new ArrayList<String>();
        for(int i = 1; i <= 3; i++) {
            String str = "allowable_" + i;
            values.add(str);
        }

        QueryParameter parameter = new QueryParameter()
            .name("test")
            .property(new StringProperty());
        
        parameter._enum(values);

        converter.validate("allowable_4", parameter);
    }
}