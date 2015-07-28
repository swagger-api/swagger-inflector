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

package io.swagger.test;

import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.RefProperty;
import io.swagger.test.models.Person;
import io.swagger.test.models.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class BodyParamExtractionTest {
    ReflectionUtils utils = new ReflectionUtils();

    @BeforeClass
    public void setup() {
        Configuration config = new Configuration();
        config.setModelPackage("io.swagger.test.models");
        config.addModelMapping("User", User.class);

        utils.setConfiguration(config);
    }

    @Test
    public void testConvertComplexBodyParamWithConfigMapping() throws Exception {
        Map<String, Model> definitions = new HashMap<String, Model>();

        Parameter parameter = new BodyParameter().schema(new RefModel("#/definitions/User"));
        Class<?> cls = utils.getParameterSignature(parameter, definitions);

        assertEquals(cls, User.class);
    }

    @Test
    public void testConvertComplexBodyParamWithoutConfigMapping() throws Exception {
        Map<String, Model> definitions = new HashMap<String, Model>();

        Parameter parameter = new BodyParameter().schema(new RefModel("#/definitions/Person"));
        Class<?> cls = utils.getParameterSignature(parameter, definitions);

        // will look up from the config model package and ref.simpleName of Person
        assertEquals(cls, Person.class);
    }

    @Test
    public void testConvertArrayBodyParam() throws Exception {
        Map<String, Model> definitions = new HashMap<String, Model>();

        Parameter parameter = new BodyParameter()
                .schema(new ArrayModel()
                        .items(new RefProperty("#/definitions/Person")));

        Class<?> cls = utils.getParameterSignature(parameter, definitions);

        // will look up from the config model package and ref.simpleName of Person
        assertEquals(cls, List.class);
    }
}
