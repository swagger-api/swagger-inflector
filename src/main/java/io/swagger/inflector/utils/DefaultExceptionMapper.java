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

package io.swagger.inflector.utils;

import io.swagger.inflector.models.ApiError;

import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    public Response toResponse(Exception exception) {
        final ApiError error = createError(ThreadLocalRandom.current().nextLong(), exception);
        final int code = error.getCode();
        if (code != Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(error.getMessage(), exception);
            }
        } else {
            LOGGER.error(error.getMessage(), exception);
        }
        return Response.status(code).entity(error).build();
    }

    private ApiError createError(long id, Exception exception) {
        if (exception instanceof ApiException) {
            return ((ApiException) exception).getError();
        } else if (exception instanceof WebApplicationException) {
            final WebApplicationException e = (WebApplicationException) exception;
            return new ApiError().code(e.getResponse().getStatus()).message(e.getMessage());
        } else {
            final String message = String.format("There was an error processing your request."
                    + " It has been logged (ID: %016x).", id);
            return new ApiError().code(Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .message(message);
        }
    }
}
