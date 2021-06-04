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

package io.swagger.oas.sample.controllers;

import io.swagger.oas.inflector.models.ApiError;
import io.swagger.oas.inflector.models.RequestContext;
import io.swagger.oas.inflector.models.ResponseContext;
import io.swagger.oas.inflector.utils.ApiException;
import io.swagger.oas.sample.models.Category;
import io.swagger.oas.sample.models.Dog;
import io.swagger.oas.sample.models.Pet;
import io.swagger.oas.sample.models.Tag;
import io.swagger.oas.test.models.Address;
import io.swagger.oas.test.models.ExtendedAddress;
import io.swagger.oas.test.models.User;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.util.List;

public class TestController {
    public ResponseContext uploadFile(RequestContext request, File inputFile, String stringMetadata, Integer intMetadata) {
        if(inputFile != null) {
            stringMetadata += ": " + String.valueOf(inputFile.length());
        }

        return new ResponseContext()
                .status(200)
                .entity(stringMetadata);
    }

    public ResponseContext uploadFilePathParam(RequestContext request, String testId, File inputFile) {
        if(inputFile != null) {
            testId += ": " + String.valueOf(inputFile.length());
        }

        return new ResponseContext()
                .status(200)
                .entity(testId);
    }

    public ResponseContext uploadFilePathParamQueryParam(RequestContext request, String testId, String testId2, String queryId, File inputFile, String stringMetadata, Integer intMetadata) {
        if(inputFile != null) {
            testId += ": " + String.valueOf(inputFile.length());
        }

        return new ResponseContext()
                .status(200)
                .entity(testId + " " + testId2 + " " + queryId + " " + stringMetadata + " " + intMetadata);
    }


    public ResponseContext inlineRequiredBody(RequestContext request, com.fasterxml.jackson.databind.JsonNode inlineBody) {
        return new ResponseContext()
            .status(200)
            .entity("success!");
    }

    public ResponseContext updatePet(RequestContext request, com.fasterxml.jackson.databind.JsonNode petType) {
        NewCookie cookie = new NewCookie("type", "chocolate");
        return new ResponseContext()
                .cookie(cookie)
                .status(200)
                .entity("OK!");
    }

    public ResponseContext disabledOuputValidation(RequestContext ctx) {
        return new ResponseContext()
                .status(200)
                .entity(new Dog());
    }

    public ResponseContext formDataTest(RequestContext request, String name, String phone, String mail, String size ,List<String>  topping, String delivery, String comments) {
        return new ResponseContext()
            .status(Status.OK)
            .contentType(MediaType.APPLICATION_JSON_TYPE)
            .entity(name);
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

    public ResponseContext withModelArray(RequestContext request, String id, Address[] modelArray) {
        return new ResponseContext()
            .status(Status.OK)
                .entity(modelArray);
    }

    public ResponseContext withValidComposedModel(RequestContext request, ExtendedAddress body) {
        if (body == null) {
            throw new NullPointerException();
        }
        return new ResponseContext().status(Status.OK);
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

    public ResponseContext binaryBody(RequestContext request,  byte[] content) {
        return new ResponseContext().status(200).entity(content);
    }

    public ResponseContext returnWithResponseHeaders(RequestContext request) {
        return new ResponseContext().status(500)
                .header("foo", "bar");
    }

    public ResponseContext mappedWithDefinedModel(RequestContext request, Dog dog) {
        return new ResponseContext().status(200);
    }

    public ResponseContext multipleMediaType(RequestContext request, Long id, String name, String dogType) {
        return new ResponseContext()
                .status(Status.OK)
                .entity(new Dog()
                        .id(id)
                        .name(name));
    }

    public ResponseContext multipleMediaTypeForNullValues(RequestContext request, Long id, String name, String dogType) {
        return new ResponseContext()
                .status(Status.OK)
                .entity(new Dog()
                        .id(id)
                        .name(name)
                        .dogType(dogType));
    }

    public ResponseContext multipleMediaTypeWithComplexValues(final RequestContext request, final Long id, final String name,
                                                              final Category category, final List<String> photoUrls,
                                                              final List<Tag> tags, final String status) {
        Pet pet = new Pet();
        pet.setId(id);
        pet.setName(name);
        pet.setCategory(category);
        pet.setPhotoUrls(photoUrls);
        pet.setTags(tags);
        pet.setStatus(status);
        return new ResponseContext()
                .status(Status.OK)
                .entity(pet);
    }

    public ResponseContext multipleMediaType(RequestContext request, Dog dog) {
        return new ResponseContext()
                .status(Status.OK)
                .entity(dog.dogType);
    }


    public ResponseContext multipleMediaType(RequestContext request, File inputFile, String string) {
        if(inputFile != null) {
            string += ": " + String.valueOf(inputFile.length());
        }

        return new ResponseContext()
                .status(200)
                .entity(string);
    }
}
