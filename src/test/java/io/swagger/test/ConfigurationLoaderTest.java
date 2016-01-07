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

package io.swagger.test;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

public class ConfigurationLoaderTest {
    @Test
    public void testLoadSampleConfiguration() throws Exception {
        System.setProperty("config", "src/test/config/config1.yaml");
        io.swagger.inflector.config.Configuration config = io.swagger.inflector.config.Configuration.read();
        assertNotNull(config);
    }
}
