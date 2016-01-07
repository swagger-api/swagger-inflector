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

package io.swagger.test.utils;

import com.google.common.collect.Lists;
import io.swagger.inflector.Constants;
import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.models.Operation;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ReflectionUtilsTest {
    ReflectionUtils utils = new ReflectionUtils();

    @BeforeClass
    public void setup() throws Exception {
        utils.setConfiguration(Configuration.defaultConfiguration());
        utils.setClassNameValidator(new ReflectionUtils.ClassNameValidator() {
            @Override
            public boolean isValidClassname(String classname) {
                return true;
            }
        });
    }

    @Test
    public void testCleanOperationId() throws Exception {
        String operationId = "hello";
        assertEquals(utils.sanitizeToJava(operationId), "hello");
    }

    @Test
    public void testOperationIdWithSpace() throws Exception {
        String operationId = "hello friends";
        assertEquals(utils.sanitizeToJava(operationId), "hello_friends");
    }

    @Test
    public void testOperationIdWithPunctuation() throws Exception {
        String operationId = "hello-my-friends and family";
        assertEquals(utils.sanitizeToJava(operationId), "hello_my_friends_and_family");
    }

    @Test
    public void testGetOperationName() throws Exception {
        Operation operation = new Operation()
          .tag("myTag");

        String controllerName = utils.getControllerName(operation);
        assertEquals(controllerName, "io.swagger.sample.controllers.MyTag");
    }

    @Test
    public void testGetOperationNameWithSpace() throws Exception {
        Operation operation = new Operation()
          .tag("my tag");

        String controllerName = utils.getControllerName(operation);
        assertEquals(controllerName, "io.swagger.sample.controllers.My_tag");
    }

    @Test
    public void testGetOperationNameWithPunctuation() throws Exception {
        Operation operation = new Operation()
          .tag("my-tags");

        String controllerName = utils.getControllerName(operation);
        assertEquals(controllerName, "io.swagger.sample.controllers.My_tags");
    }
    
    @Test
    public void testGetOperationNameWithWhitespace() throws Exception {
        Operation operation = new Operation()
          .tag(" myTags ");

        String controllerName = utils.getControllerName(operation);
        assertEquals(controllerName, "io.swagger.sample.controllers.MyTags");
    }

    @Test
    public void testGetControllerNameFromExtension() throws Exception {
        Operation operation = new Operation();
        operation.setVendorExtension(Constants.X_SWAGGER_ROUTER_CONTROLLER, "com.test.class");

        String controllerName = utils.getControllerName(operation);
        assertEquals(controllerName, "com.test.class");
    }

    @Test
    public void testGetControllerNameFromConfig() throws Exception {
        Operation operation = new Operation();

        String controllerName = utils.getControllerName(operation);
        assertEquals(controllerName, "io.swagger.sample.controllers.Default");

        utils.getConfiguration().setControllerClass( "MyController");

        controllerName = utils.getControllerName(operation);
        assertEquals(controllerName, "io.swagger.sample.controllers.MyController");
    }

    @Test
    public void testGetControllerNameWithMultipleTags() throws Exception {

        ReflectionUtils utils = new ReflectionUtils();
        utils.setConfiguration( Configuration.defaultConfiguration());

        ReflectionUtils.ClassNameValidator verifier = mock(ReflectionUtils.ClassNameValidator.class);
        utils.setClassNameValidator( verifier );

        Configuration configuration = utils.getConfiguration();
        when( verifier.isValidClassname( configuration.getControllerPackage() + ".Two")).thenReturn( true );

        Operation operation = new Operation().tags(Lists.newArrayList("one", " two "));
        String controllerName = utils.getControllerName(operation);
        assertEquals(controllerName, "io.swagger.sample.controllers.Two");

        when( verifier.isValidClassname( configuration.getControllerPackage() + ".Two")).thenReturn( false );
        when( verifier.isValidClassname( configuration.getControllerPackage() + ".OneController")).thenReturn( true );

        operation = new Operation().tags(Lists.newArrayList("one", " two "));
        controllerName = utils.getControllerName(operation);
        assertEquals(controllerName, "io.swagger.sample.controllers.OneController");

        when( verifier.isValidClassname( configuration.getControllerPackage() + ".OneController")).thenReturn( false );

        operation = new Operation().tags(Lists.newArrayList("one", " two "));
        controllerName = utils.getControllerName(operation);
        assertEquals(controllerName, "io.swagger.sample.controllers.Default");
    }
}