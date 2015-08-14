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

package io.swagger.test.utils;

import io.swagger.inflector.utils.ReflectionUtils;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ReflectionUtilsTest {
    ReflectionUtils utils = new ReflectionUtils();

    @Test
    public void testCleanOperationId() throws Exception {
        String operationId = "hello";
        assertEquals(utils.sanitizeOperationId(operationId), "hello");
    }

    @Test
    public void testOperationIdWithSpace() throws Exception {
        String operationId = "hello friends";
        assertEquals(utils.sanitizeOperationId(operationId), "hello_friends");
    }

    @Test
    public void testOperationIdWithPunctuation() throws Exception {
        String operationId = "hello-my-friends and family";
        assertEquals(utils.sanitizeOperationId(operationId), "hello_my_friends_and_family");
    }
}