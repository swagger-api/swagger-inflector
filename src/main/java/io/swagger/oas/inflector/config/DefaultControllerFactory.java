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

package io.swagger.oas.inflector.config;


import io.swagger.v3.oas.models.Operation;

/**
 * Default ControllerFactory implementation that just calls newInstance
 */

public class DefaultControllerFactory implements ControllerFactory {

    /**
     * Instantiates the provided class calling cls.newInstance()
     *
     * @param cls the class to be instantiated
     * @param operation the operation to instantiate
     * @return an instance of the provided class
     * @throws IllegalAccessException The class cannot be initialized
     * @throws InstantiationException The class cannot be initialized
     */

    @Override
    public Object instantiateController(Class<? extends Object> cls, Operation operation) throws IllegalAccessException, InstantiationException {
        return cls.newInstance();
    }
}
