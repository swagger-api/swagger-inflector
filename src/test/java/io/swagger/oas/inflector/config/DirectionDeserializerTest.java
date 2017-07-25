/*
 * Copyright 2017 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.swagger.oas.inflector.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.util.Yaml;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.Set;

public class DirectionDeserializerTest {
    private static final String BOOLEAN = "boolean";
    private static final String BAD = "bad";

    @Test
    public void testFromArray() throws Exception {
        try (InputStream in = DirectionDeserializerTest.class.getResourceAsStream(
                "validation-as-set.yaml")) {
            final Holder holder = Yaml.mapper().readValue(in, Holder.class);
            Assert.assertEquals(holder.getValue(), EnumSet.of(Configuration.Direction.IN));
        }
    }

    @Test(dataProvider = BOOLEAN)
    public void testFromBoolean(String source,
            Set<Configuration.Direction> expected) throws Exception {
        try (InputStream in = DirectionDeserializerTest.class.getResourceAsStream(source)) {
            final Holder holder = Yaml.mapper().readValue(in, Holder.class);
            Assert.assertEquals(holder.getValue(), expected);
        }
    }

    @Test(dataProvider = BAD, expectedExceptions =  {JsonMappingException.class})
    public void testFailures(String source) throws Exception {
        try (InputStream in = DirectionDeserializerTest.class.getResourceAsStream(source)) {
            Yaml.mapper().readValue(in, Holder.class);
        }
    }

    @DataProvider(name = BOOLEAN)
    private Object[][] listFilesWithBoolean() {
        return new Object[][] {
                {"validation-as-false.yaml", EnumSet.noneOf(Configuration.Direction.class)},
                {"validation-as-true.yaml", EnumSet.allOf(Configuration.Direction.class)}
        };
    }

    @DataProvider(name = BAD)
    private Object[][] listBadFiles() {
        return new Object[][] {
                {"validation-as-incomplete.yaml"},
                {"validation-as-object.yaml"},
                {"validation-as-object-array.yaml"},
                {"validation-as-scalar.yaml"},
                {"validation-as-string-array.yaml"},
        };
    }

    private static class Holder {
        private Set<Configuration.Direction> value;

        Set<Configuration.Direction> getValue() {
            return value;
        }

        @JsonDeserialize(using = DirectionDeserializer.class)
        public void setValue(Set<Configuration.Direction> value) {
            this.value = value;
        }
    }
}
