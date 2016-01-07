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

package io.swagger.test.integration.responses;

import io.swagger.test.models.User;
import io.swagger.test.client.ApiClient;

import io.swagger.util.Json;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;

public class OverloadedResponseTestIT {
    @Test
    public void verifyOverloadedMethod() throws Exception {
        ApiClient client = new ApiClient();

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("arg1", "test1");

        String str = client.invokeAPI("/overloaded", "GET", queryParams, null, new HashMap<String, String>(), null, "application/json", null, new String[0]);
        User user = Json.mapper().readValue(str, User.class);

        assertNotNull(user);
        Json.prettyPrint(user);
    }
}
