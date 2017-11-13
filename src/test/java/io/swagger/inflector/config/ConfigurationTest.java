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
package io.swagger.inflector.config;

import io.swagger.inflector.SwaggerInflector;
import io.swagger.models.Operation;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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
    public void defaultSchemaFactoryProviderTest() {
        assertEquals(new Configuration().getSchemaFactoryProviderClass(), DefaultJsonSchemaFactoryProvider.class.getName());
        assertEquals(new Configuration().getSchemaFactoryProvider().getClass(), DefaultJsonSchemaFactoryProvider.class);
        assertNotNull(new Configuration().getSchemaFactory());
    }

    @Test
    public void schemaFactoryProviderFromClassNameTest() {
        final Configuration configuration = new Configuration();
        configuration.setSchemaFactoryProviderClass(JsonSchemaFactoryProviderMock.class.getName());

        assertEquals(configuration.getSchemaFactoryProvider().getClass(), JsonSchemaFactoryProviderMock.class);
        assertEquals(configuration.getSchemaFactory(),
            ((JsonSchemaFactoryProviderMock)configuration.getSchemaFactoryProvider()).jsonSchemaFactory);
    }

    @Test
    public void schemaFactoryProviderFromInstanceNameTest() {
        final Configuration configuration = new Configuration();
        configuration.setSchemaFactoryProvider(new JsonSchemaFactoryProviderMock());

        assertEquals(configuration.getSchemaFactoryProvider().getClass(), JsonSchemaFactoryProviderMock.class);
        assertEquals(configuration.getSchemaFactoryProviderClass(), JsonSchemaFactoryProviderMock.class.getName());
    }

    @Test
    public void testSlashBasePath() {
        String basePath = "/";
        String swaggerBase = "/bar";
        assertEquals(SwaggerInflector.basePath(basePath, swaggerBase), "/bar/");
    }

    @Test
    public void testEmptyBasePath() {
        String basePath = "";
        String swaggerBase = "/bar";
        assertEquals(SwaggerInflector.basePath(basePath, swaggerBase), "/bar/");
    }

    @Test
    public void testPopulatedParts() {
        String basePath = "/v1";
        String swaggerBase = "/bar";
        assertEquals(SwaggerInflector.basePath(basePath, swaggerBase), "/v1/bar/");
    }

    @Test
    public void testEmptySwaggerBase() {
        String basePath = "/";
        String swaggerBase = "";
        assertEquals(SwaggerInflector.basePath(basePath, swaggerBase), "/");
    }

    @Test
    public void testEmptyParts() {
        String basePath = "";
        String swaggerBase = "";
        assertEquals(SwaggerInflector.basePath(basePath, swaggerBase), "/");
    }

    @Test
    public void testTrailingBasePathSlash() {
        String basePath = "/api/";
        String swaggerBase = "";
        assertEquals(SwaggerInflector.basePath(basePath, swaggerBase), "/api/");
    }

    @Test
    public void testTrailingSwaggerBasePathSlash() {
        String basePath = "/api";
        String swaggerBase = "/foo/";
        assertEquals(SwaggerInflector.basePath(basePath, swaggerBase), "/api/foo/");
    }

    @Test
    public void testTrailingSlashes() {
        String basePath = "/api/";
        String swaggerBase = "/foo/";
        assertEquals(SwaggerInflector.basePath(basePath, swaggerBase), "/api/foo/");
    }

    public static class ControllerFactoryImpl implements ControllerFactory {
        @Override
        public Object instantiateController(Class<? extends Object> cls,
                Operation operation) throws IllegalAccessException, InstantiationException {
            return null;
        }
    }

}
