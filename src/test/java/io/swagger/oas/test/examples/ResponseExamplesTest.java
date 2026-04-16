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
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.OpenAPI;


import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import org.testng.annotations.Test;


import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ResponseExamplesTest {
    private int equal = 0;
    private int different = 0;
    private List<io.swagger.v3.parser.core.models.AuthorizationValue> auths = mock(List.class);

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
        List<String> exampleProcessor = new ArrayList<>();
        exampleProcessor.add("sequence");
        config.setExampleProcessors(exampleProcessor);
        ParseOptions options = new ParseOptions();
        options.setResolve(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readLocation( "src/test/swagger/oas3.yaml",auths, options).getOpenAPI();
        Operation operation = openAPI.getPaths().get( "/mockResponses/responseWithExamples").getGet();

       OpenAPIOperationController controller = new OpenAPIOperationController(
            config, "/mockResponses/responseWithExamples", "GET", operation,"" ,openAPI.getComponents().getSchemas() );

        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock( UriInfo.class );

        when( uriInfo.getPath()).thenReturn( "/mockResponses/responseWithExamples");
        when( uriInfo.getQueryParameters()).thenReturn( new MultivaluedHashMap<String, String>());
        when( uriInfo.getPathParameters()).thenReturn( new MultivaluedHashMap<String, String>());

        when( requestContext.getAcceptableMediaTypes()).thenReturn(Arrays.asList(MediaType.APPLICATION_JSON_TYPE));
        when( requestContext.getHeaders()).thenReturn( new MultivaluedHashMap<String, String>());
        when( requestContext.getUriInfo()).thenReturn( uriInfo );

        Response response = controller.apply( requestContext );

        assertEquals( response.getStatus(), 200 );
        assertEquals(  Json.mapper().writeValueAsString(response.getEntity()), "{\"value\":{\"test\":\"jsonvalue\"}}");
    }

    @Test
    public void testResponseYamlExample() throws Exception {

        Configuration config = new Configuration();
        List<String> exampleProcessor = new ArrayList<>();
        exampleProcessor.add("random");
        config.setExampleProcessors(exampleProcessor);
        ParseOptions options = new ParseOptions();
        options.setResolve(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readLocation( "src/test/swagger/oas3.yaml",auths, options).getOpenAPI();
        Operation operation = openAPI.getPaths().get( "/mockResponses/responseWithExamples").getGet();

        OpenAPIOperationController controller = new OpenAPIOperationController(
            config, "/mockResponses/responseWithExamples", "GET", operation,"" ,openAPI.getComponents().getSchemas());

        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock( UriInfo.class );

        when( uriInfo.getPath()).thenReturn( "/mockResponses/responseWithExamples");
        when( uriInfo.getQueryParameters()).thenReturn( new MultivaluedHashMap<String, String>());
        when( uriInfo.getPathParameters()).thenReturn( new MultivaluedHashMap<String, String>());

        when( requestContext.getAcceptableMediaTypes()).thenReturn(Arrays.asList(MediaType.valueOf("application/yaml")));
        when( requestContext.getHeaders()).thenReturn( new MultivaluedHashMap<String, String>());
        when( requestContext.getUriInfo()).thenReturn( uriInfo );

        Response response = controller.apply( requestContext );

        assertEquals( response.getStatus(), 200 );
        assertEqualsIgnoreLineEnding(  Yaml.mapper().writeValueAsString(response.getEntity()), "value:  test: yamlvalue");
    }

    private void assertEqualsIgnoreLineEnding(String actual, String expected) {
        assertEquals(actual.replace("\n", ""), expected);
    }

    private void testRandomJsonExample() throws Exception {
        Configuration config = new Configuration();
        List<String> exampleProcessor = new ArrayList<>();
        exampleProcessor.add("random");
        config.setExampleProcessors(exampleProcessor);
        ParseOptions options = new ParseOptions();
        options.setResolveFully(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readLocation( "src/test/swagger/oas3.yaml",auths, options).getOpenAPI();
        Operation operation = openAPI.getPaths().get( "/mockResponses/objectMultipleExamples").getGet();

        OpenAPIOperationController controller = new OpenAPIOperationController(
                config, "/mockResponses/objectMultipleExamples", "GET", operation,"" ,openAPI.getComponents().getSchemas() );

        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock( UriInfo.class );

        when( uriInfo.getPath()).thenReturn( "/mockResponses/objectMultipleExamples");
        when( uriInfo.getQueryParameters()).thenReturn( new MultivaluedHashMap<String, String>());
        when( uriInfo.getPathParameters()).thenReturn( new MultivaluedHashMap<String, String>());

        when( requestContext.getAcceptableMediaTypes()).thenReturn(Arrays.asList(MediaType.APPLICATION_JSON_TYPE));
        when( requestContext.getHeaders()).thenReturn( new MultivaluedHashMap<String, String>());
        when( requestContext.getUriInfo()).thenReturn( uriInfo );

        Response response = controller.apply( requestContext );

        assertEquals( response.getStatus(), 200 );
        io.swagger.v3.oas.models.examples.Example example1 = (Example) response.getEntity();
        assertNotNull( Json.mapper().writeValueAsString(example1));

        Response response1 = controller.apply( requestContext );

        assertEquals( response1.getStatus(), 200 );
        io.swagger.v3.oas.models.examples.Example example2 = (Example) response1.getEntity();
        assertNotNull( Json.mapper().writeValueAsString(example2));


        if(example1 != example2) {
            different++;
            assertNotEquals(example1, example2);
        }else{
            equal++;
            assertEquals(example1, example2);
        }
    }

    @Test
    public void testRandom() throws Exception {
        for (int i = 0;i <1000; i++) {
            testRandomJsonExample();
        }
        if(different>equal) {
            assertTrue(different > equal);
        }else if(different<equal) {
            fail("different:" + different + " equal: " + equal);
        }
    }

    @Test
    public void testSecuenceJsonExample() throws Exception {
        Configuration config = new Configuration();
        List<String> exampleProcessor = new ArrayList<>();
        exampleProcessor.add("sequence");
        config.setExampleProcessors(exampleProcessor);
        ParseOptions options = new ParseOptions();
        options.setResolveFully(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readLocation( "src/test/swagger/oas3.yaml",auths, options).getOpenAPI();
        Operation operation = openAPI.getPaths().get( "/mockResponses/objectMultipleExamples").getGet();

        OpenAPIOperationController controller = new OpenAPIOperationController(
                config, "/mockResponses/objectMultipleExamples", "GET", operation,"" ,openAPI.getComponents().getSchemas() );

        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock( UriInfo.class );

        when( uriInfo.getPath()).thenReturn( "/mockResponses/objectMultipleExamples");
        when( uriInfo.getQueryParameters()).thenReturn( new MultivaluedHashMap<String, String>());
        when( uriInfo.getPathParameters()).thenReturn( new MultivaluedHashMap<String, String>());

        when( requestContext.getAcceptableMediaTypes()).thenReturn(Arrays.asList(MediaType.APPLICATION_JSON_TYPE));
        when( requestContext.getHeaders()).thenReturn( new MultivaluedHashMap<String, String>());
        when( requestContext.getUriInfo()).thenReturn( uriInfo );

        Response response = controller.apply( requestContext );

        assertEquals( response.getStatus(), 200 );
        io.swagger.v3.oas.models.examples.Example example1 = (Example) response.getEntity();
        assertEquals( Json.mapper().writeValueAsString(example1), "{\"value\":{\"id\":6,\"name\":\"Queen Victoria\"}}");

        Response response1 = controller.apply( requestContext );

        assertEquals( response1.getStatus(), 200 );
        io.swagger.v3.oas.models.examples.Example example2 = (Example) response1.getEntity();
        assertEquals( Json.mapper().writeValueAsString(example2), "{\"value\":{\"id\":5,\"name\":\"Grace Gonzalez\"}}");

        Response response2 = controller.apply( requestContext );

        assertEquals( response2.getStatus(), 200 );
        io.swagger.v3.oas.models.examples.Example example3 = (Example) response2.getEntity();
        assertEquals( Json.mapper().writeValueAsString(example3), "{\"value\":{\"id\":4,\"name\":\"Arthur Dent\"}}");

        Response response3 = controller.apply( requestContext );

        assertEquals( response3.getStatus(), 200 );
        io.swagger.v3.oas.models.examples.Example example4 = (Example) response3.getEntity();
        assertEquals( Json.mapper().writeValueAsString(example4), "{\"value\":{\"id\":3,\"name\":\"Tricia McMillan\"}}");

        Response response4 = controller.apply( requestContext );

        assertEquals( response4.getStatus(), 200 );
        io.swagger.v3.oas.models.examples.Example example5 = (Example) response.getEntity();
        assertEquals( Json.mapper().writeValueAsString(example5), "{\"value\":{\"id\":6,\"name\":\"Queen Victoria\"}}");

    }

    @Test
    public void testRandomRequestedJsonExample() {
        Configuration config = new Configuration();
        List<String> exampleProcessor = new ArrayList<>();
        exampleProcessor.add("random");
        config.setExampleProcessors(exampleProcessor);
        ParseOptions options = new ParseOptions();
        options.setResolveFully(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readLocation( "src/test/swagger/oas3.yaml",this.auths, options).getOpenAPI();
        Operation operation = openAPI.getPaths().get( "/mockResponses/objectMultipleExamples").getGet();

        OpenAPIOperationController controller = new OpenAPIOperationController(
                config, "/mockResponses/objectMultipleExamples", "GET",operation,"", openAPI.getComponents().getSchemas() );

        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock( UriInfo.class );

        when( uriInfo.getPath()).thenReturn( "/mockResponses/objectMultipleExamples");
        when( uriInfo.getQueryParameters()).thenReturn( new MultivaluedHashMap<String, String>());
        when( uriInfo.getPathParameters()).thenReturn( new MultivaluedHashMap<String, String>());


        when( requestContext.getHeaders()).thenReturn( new MultivaluedHashMap<String, String>());
        requestContext.getHeaders().add("Accept","application/json");
        when( requestContext.getUriInfo()).thenReturn( uriInfo );

        Response response = controller.apply( requestContext );

        assertEquals( response.getStatus(), 200 );
        assertEquals( response.getMediaType().getSubtype(), "json" );

        assertNotNull( response.getEntity());

    }


    @Test
    public void testRandomRequestedXmlExample() throws Exception {
        Configuration config = new Configuration();
        List<String> exampleProcessor = new ArrayList<>();
        exampleProcessor.add("random");
        config.setExampleProcessors(exampleProcessor);
        ParseOptions options = new ParseOptions();
        options.setResolveFully(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readLocation( "src/test/swagger/oas3.yaml",this.auths, options).getOpenAPI();
        Operation operation = openAPI.getPaths().get( "/mockResponses/objectMultipleExamples").getGet();

        OpenAPIOperationController controller = new OpenAPIOperationController(
                config, "/mockResponses/objectMultipleExamples", "GET", operation, "" ,openAPI.getComponents().getSchemas() );

        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock( UriInfo.class );

        when( uriInfo.getPath()).thenReturn( "/mockResponses/objectMultipleExamples");
        when( uriInfo.getQueryParameters()).thenReturn( new MultivaluedHashMap<String, String>());
        when( uriInfo.getPathParameters()).thenReturn( new MultivaluedHashMap<String, String>());


        when( requestContext.getHeaders()).thenReturn( new MultivaluedHashMap<String, String>());
        requestContext.getHeaders().add("Accept","application/xml");
        when( requestContext.getUriInfo()).thenReturn( uriInfo );

        Response response = controller.apply( requestContext );

        assertEquals( response.getStatus(), 200 );
        assertEquals( response.getMediaType().getSubtype(), "xml" );

        assertNotNull( response.getEntity());

    }

}
