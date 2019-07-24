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

package io.swagger.oas.test;

import org.testng.annotations.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;

public class ConfigurationLoaderTest {
    @Test
    public void testLoadSampleConfiguration() throws Exception {
        System.setProperty("config", "src/test/config/config2.yaml");
        io.swagger.oas.inflector.config.Configuration config = io.swagger.oas.inflector.config.Configuration.read();
        assertNotNull(config);
        assertTrue(config.getExposedSpecOptions().getParseOptions().isResolve());
        assertFalse(config.getExposedSpecOptions().isHideInflectorExtensions());
    }

    @Test
    public void testExposedSpecFromFileDefaults() throws Exception {
        System.setProperty("config", "src/test/config/config1.yaml");
        io.swagger.oas.inflector.config.Configuration config = io.swagger.oas.inflector.config.Configuration.read();
        assertNotNull(config);
        assertFalse(config.getExposedSpecOptions().getParseOptions().isResolve());
        assertTrue(config.getExposedSpecOptions().isHideInflectorExtensions());
    }


}
