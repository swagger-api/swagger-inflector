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

package io.swagger.test.examples;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.module.SimpleModule;

import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.controllers.SwaggerOperationController;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

public class ResponseExamplesTest {

    static {
        // register the JSON serializer
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonNodeExampleSerializer());
        Json.mapper().registerModule(simpleModule);
        Yaml.mapper().registerModule(simpleModule);
    }

    @Test
    public void testResponseJsonExample() throws Exception {
        Configuration config = new Configuration();
        Swagger swagger = new SwaggerParser().read( "src/test/swagger/sample1.yaml");
        Operation operation = swagger.getPath( "/mockResponses/responseWithExamples").getGet();

        SwaggerOperationController controller = new SwaggerOperationController(swagger,
            config, "/mockResponses/responseWithExamples", "GET", operation, swagger.getDefinitions() );

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
        assertEquals( "{\"test\":\"jsonvalue\"}", Json.mapper().writeValueAsString(response.getEntity()));
    }

    @Test
    public void testResponseYamlExample() throws Exception {

        Configuration config = new Configuration();
        Swagger swagger = new SwaggerParser().read( "src/test/swagger/sample1.yaml");
        Operation operation = swagger.getPath( "/mockResponses/responseWithExamples").getGet();

        SwaggerOperationController controller = new SwaggerOperationController(swagger,
            config, "/mockResponses/responseWithExamples", "GET", operation, swagger.getDefinitions() );

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
        assertEquals( "---\ntest: \"yamlvalue\"\n", Yaml.mapper().writeValueAsString(response.getEntity()));
    }
}
