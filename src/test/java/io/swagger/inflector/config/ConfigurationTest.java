/*
 * Copyright 2016 SmartBear Software
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

import io.swagger.models.Operation;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigurationTest {

    @Test
    public void defaultControllerFactoryTest() {
        Assert.assertEquals(new Configuration().getControllerFactory().getClass(), DefaultControllerFactory.class);
    }

    @Test
    public void controllerFactoryFromClassNameTest() {
        final Configuration configuration = new Configuration();
        configuration.setControllerFactoryClass(ControllerFactoryImpl.class.getName());

        Assert.assertEquals(configuration.getControllerFactory().getClass(), ControllerFactoryImpl.class);
    }

    @Test
    public void controllerFactoryFromInstanceNameTest() {
        final Configuration configuration = new Configuration();
        configuration.setControllerFactory(new ControllerFactoryImpl());

        Assert.assertEquals(configuration.getControllerFactory().getClass(), ControllerFactoryImpl.class);
    }

    public static class ControllerFactoryImpl implements ControllerFactory {
        @Override
        public Object instantiateController(Class<? extends Object> cls,
                Operation operation) throws IllegalAccessException, InstantiationException {
            return null;
        }
    }

}
