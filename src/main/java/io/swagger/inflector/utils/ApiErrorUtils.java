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

package io.swagger.inflector.utils;

import io.swagger.inflector.models.ApiError;

import javax.ws.rs.core.Response;
import java.util.concurrent.ThreadLocalRandom;

public class ApiErrorUtils {

    public static ApiError createInternalError() {
        final String message = String.format("There was an error processing your request."
                + " It has been logged (ID: %016x)", ThreadLocalRandom.current().nextLong());
        return new ApiError().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .message(message);
    }
}
