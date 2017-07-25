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

import io.swagger.oas.inflector.OpenAPIInflector;
import io.swagger.oas.models.Operation;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ConfigurationTest {

    @Test
    public void defaultControllerFactoryTest() {
        assertEquals(new Configuration().getControllerFactory().getClass(), DefaultControllerFactory.class);
    }

    @Test
    public void controllerFactoryFromClassNameTest() {
        final Configuration configuration = new Configuration();
        configuration.setControllerFactoryClass(ControllerFactoryImpl.class.getName());

        assertEquals(configuration.getControllerFactory().getClass(), ControllerFactoryImpl.class);
    }

    @Test
    public void controllerFactoryFromInstanceNameTest() {
        final Configuration configuration = new Configuration();
        configuration.setControllerFactory(new ControllerFactoryImpl());

        assertEquals(configuration.getControllerFactory().getClass(), ControllerFactoryImpl.class);
    }

    @Test
    public void testSlashBasePath() {
        String basePath = "/";
        String swaggerBase = "/bar";
        Assert.assertEquals(OpenAPIInflector.basePath(basePath, swaggerBase), "/bar/");
    }

    @Test
    public void testEmptyBasePath() {
        String basePath = "";
        String swaggerBase = "/bar";
        assertEquals(OpenAPIInflector.basePath(basePath, swaggerBase), "/bar/");
    }

    @Test
    public void testPopulatedParts() {
        String basePath = "/v1";
        String swaggerBase = "/bar";
        assertEquals(OpenAPIInflector.basePath(basePath, swaggerBase), "/v1/bar/");
    }

    @Test
    public void testEmptySwaggerBase() {
        String basePath = "/";
        String swaggerBase = "";
        assertEquals(OpenAPIInflector.basePath(basePath, swaggerBase), "/");
    }

    @Test
    public void testEmptyParts() {
        String basePath = "";
        String swaggerBase = "";
        assertEquals(OpenAPIInflector.basePath(basePath, swaggerBase), "/");
    }

    @Test
    public void testTrailingBasePathSlash() {
        String basePath = "/api/";
        String swaggerBase = "";
        assertEquals(OpenAPIInflector.basePath(basePath, swaggerBase), "/api/");
    }

    @Test
    public void testTrailingSwaggerBasePathSlash() {
        String basePath = "/api";
        String swaggerBase = "/foo/";
        assertEquals(OpenAPIInflector.basePath(basePath, swaggerBase), "/api/foo/");
    }

    @Test
    public void testTrailingSlashes() {
        String basePath = "/api/";
        String swaggerBase = "/foo/";
        assertEquals(OpenAPIInflector.basePath(basePath, swaggerBase), "/api/foo/");
    }

    public static class ControllerFactoryImpl implements ControllerFactory {
        @Override
        public Object instantiateController(Class<? extends Object> cls,
                Operation operation) throws IllegalAccessException, InstantiationException {
            return null;
        }
    }

}
