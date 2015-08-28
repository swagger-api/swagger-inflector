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

package io.swagger.sample.controllers;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import io.swagger.test.models.Address;
import io.swagger.test.models.User;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

public class TestController {
    public ResponseContext formTest(RequestContext request, String user) {
      System.out.println("found it! " + user);
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

    public ResponseContext withModel(RequestContext request, String id, Address animal) {
        return new ResponseContext()
            .status(Status.OK)
            .entity("ok");
    }

    public ResponseContext withModelArray(RequestContext request, String id, Address[] modelArray) {
        return new ResponseContext()
            .status(Status.OK);
    }
}