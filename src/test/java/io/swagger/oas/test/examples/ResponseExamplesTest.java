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

package io.swagger.oas.test.examples;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.oas.inflector.config.Configuration;
import io.swagger.oas.inflector.controllers.OpenAPIOperationController;

import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.OpenAPI;

import io.swagger.parser.models.ParseOptions;
import io.swagger.parser.v3.OpenAPIV3Parser;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import mockit.Injectable;
import org.testng.annotations.Test;


import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.testng.Assert.assertEquals;

public class ResponseExamplesTest {

    static {
        // register the JSON serializer
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonNodeExampleSerializer());
        Json.mapper().registerModule(simpleModule);
        Yaml.mapper().registerModule(simpleModule);
    }

    @Test
    public void testResponseJsonExample(@Injectable final List<io.swagger.parser.models.AuthorizationValue> auths) throws Exception {
        Configuration config = new Configuration();
        ParseOptions options = new ParseOptions();
        options.setResolve(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readLocation( "src/test/swagger/oas3.yaml",auths, options).getOpenAPI();
        Operation operation = openAPI.getPaths().get( "/mockResponses/responseWithExamples").getGet();

       OpenAPIOperationController controller = new OpenAPIOperationController(
            config, "/mockResponses/responseWithExamples", "GET", operation, openAPI.getComponents().getSchemas() );

        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock( UriInfo.class );

        stub( uriInfo.getPath()).toReturn( "/mockResponses/responseWithExamples");
        stub( uriInfo.getQueryParameters()).toReturn( new MultivaluedHashMap<String, String>());
        stub( uriInfo.getPathParameters()).toReturn( new MultivaluedHashMap<String, String>());

        stub( requestContext.getAcceptableMediaTypes()).toReturn(Arrays.asList(MediaType.APPLICATION_JSON_TYPE));
        stub( requestContext.getHeaders()).toReturn( new MultivaluedHashMap<String, String>());
        stub( requestContext.getUriInfo()).toReturn( uriInfo );

        Response response = controller.apply( requestContext );

        assertEquals( 200, response.getStatus() );
        assertEquals(  Json.mapper().writeValueAsString(response.getEntity()), "{\"value\":\"{\\\"test\\\":\\\"jsonvalue\\\"}\"}");
    }

    @Test
    public void testResponseYamlExample(@Injectable final List<io.swagger.parser.models.AuthorizationValue> auths) throws Exception {

        Configuration config = new Configuration();
        ParseOptions options = new ParseOptions();
        options.setResolve(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readLocation( "src/test/swagger/oas3.yaml",auths, options).getOpenAPI();
        Operation operation = openAPI.getPaths().get( "/mockResponses/responseWithExamples").getGet();

        OpenAPIOperationController controller = new OpenAPIOperationController(
            config, "/mockResponses/responseWithExamples", "GET", operation, openAPI.getComponents().getSchemas());

        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock( UriInfo.class );

        stub( uriInfo.getPath()).toReturn( "/mockResponses/responseWithExamples");
        stub( uriInfo.getQueryParameters()).toReturn( new MultivaluedHashMap<String, String>());
        stub( uriInfo.getPathParameters()).toReturn( new MultivaluedHashMap<String, String>());

        stub( requestContext.getAcceptableMediaTypes()).toReturn(Arrays.asList(MediaType.valueOf("application/yaml")));
        stub( requestContext.getHeaders()).toReturn( new MultivaluedHashMap<String, String>());
        stub( requestContext.getUriInfo()).toReturn( uriInfo );

        Response response = controller.apply( requestContext );

        assertEquals( 200, response.getStatus() );
        assertEqualsIgnoreLineEnding(  Yaml.mapper().writeValueAsString(response.getEntity()), "value: '{\"test\":\"yamlvalue\"}'");
    }

    private void assertEqualsIgnoreLineEnding(String actual, String expected) {
        assertEquals(actual.replace("\n", ""), expected);
    }
}
