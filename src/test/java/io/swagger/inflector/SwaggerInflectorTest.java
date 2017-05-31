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
package io.swagger.inflector;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.processors.JsonProvider;
import io.swagger.util.Json;


public class SwaggerInflectorTest {

    Configuration config;

    @BeforeTest
    public void before()
    {
        System.setProperty("config", "src/test/config/config1.yaml");
        config = Configuration.read();
    }

    @Test
    public void testLoadWithDefaultObjectMapper() throws Exception {
        SwaggerInflector inflector = new SwaggerInflector(config);
        assertEquals(Json.mapper(),inflector.getObjectMapper());
        assertTrue(inflector.isRegistered(JsonProvider.class));
    }


    @Test
    public void testLoadWithCustomObjectMapper() throws Exception {
        // ensure that pretty print is enabled
        config.setPrettyPrint(true);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        SwaggerInflector inflector = new SwaggerInflector(config,objectMapper);
        assertEquals(objectMapper,inflector.getObjectMapper());
        assertNotEquals(Json.mapper(),inflector.getObjectMapper());
        // This class SHOULD NOT be registered since the custom mapper
        // is providing this functionality.
        assertFalse(inflector.isRegistered(JsonProvider.class));
    }
}
