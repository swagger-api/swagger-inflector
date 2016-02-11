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

package io.swagger.sample.controllers;

import io.swagger.inflector.models.ApiError;
import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import io.swagger.inflector.utils.ApiException;
import io.swagger.test.models.Address;
import io.swagger.sample.models.Dog;
import io.swagger.test.models.User;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

public class TestController {
    public io.swagger.inflector.models.ResponseContext inlineRequiredBody(io.swagger.inflector.models.RequestContext request, com.fasterxml.jackson.databind.JsonNode inlineBody) {
        return new ResponseContext()
            .status(200)
            .entity("success!");
    }

    public ResponseContext formTest(RequestContext request, String user) {
        return new ResponseContext()
            .status(Status.OK)
            .contentType(MediaType.APPLICATION_JSON_TYPE)
            .entity(user);
    }

    public ResponseContext postFormData(RequestContext request, Long id, String name) {
        // show a sample response
        return new ResponseContext()
            .status(Status.OK)
            .contentType(MediaType.APPLICATION_JSON_TYPE)
            .entity(new User()
                .id(id)
                .user(name));
    }

    public ResponseContext overloadedResponse(RequestContext request, String arg1) {
        return new ResponseContext()
            .status(Status.OK)
            .entity(new User());
    }

    public ResponseContext withModel(RequestContext request, String id, Address address) {
        if("-1".equals(id)) {
            return new ResponseContext()
                    .status(Status.OK)
                    .entity("oops!  This isn't valid!");
        }
        else {
            if(address == null) {
                address = new Address();
            }
            address.setStreet(id + " street");
            return new ResponseContext()
                    .status(Status.OK)
                    .entity(address);
        }
    }

    public ResponseContext withModelArray(RequestContext request, String id, List<Address> modelArray) {
        return new ResponseContext()
            .status(Status.OK);
    }

    public ResponseContext arrayInputTest(RequestContext request, List<String> users) {
        return new ResponseContext()
            .status(Status.OK)
            .entity(users);
    }

    public ResponseContext throwApiException(RequestContext request) {
        final Response.Status status = Response.Status.CONFLICT;
        throw new ApiException(
                new ApiError().code(status.getStatusCode()).message(status.getReasonPhrase()));
    }

    public ResponseContext throwApiExceptionAsCause(RequestContext request) {
        final Response.Status status = Response.Status.CONFLICT;
        throw new RuntimeException(new ApiException(
                new ApiError().code(status.getStatusCode()).message(status.getReasonPhrase())));
    }

    public ResponseContext throwNonApiException(RequestContext request) {
        throw new NullPointerException("I'm NPE!");
    }

    public ResponseContext returnNonRfc2616Status(RequestContext request) {
        return new ResponseContext().status(422).entity("I'm from RFC 4918");
    }

    public ResponseContext stringBody(RequestContext request, String body) {
        return new ResponseContext().status(200).entity(body);
    }

    public ResponseContext returnWithResponseHeaders(RequestContext request) {
        return new ResponseContext().status(500)
                .header("foo", "bar");
    }

    public ResponseContext mappedWithDefinedModel(RequestContext request, Dog dog) {
        return new ResponseContext().status(200);
    }
}
