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

package io.swagger.oas.inflector.utils;

import io.swagger.oas.inflector.models.ApiError;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApiErrorUtilsTest {
    @Test
    public void testInternalError() {
        final ApiError first = ApiErrorUtils.createInternalError();
        Assert.assertEquals(first.getCode(), 500);
        final ApiError second = ApiErrorUtils.createInternalError();
        Assert.assertEquals(second.getCode(), first.getCode());
        Assert.assertNotEquals(second.getMessage(), first.getMessage());
    }
}
