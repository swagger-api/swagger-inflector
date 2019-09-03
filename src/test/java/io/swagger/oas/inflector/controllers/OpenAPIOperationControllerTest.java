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

package io.swagger.oas.inflector.controllers;

import com.google.common.collect.Maps;
import io.swagger.oas.inflector.config.Configuration;
import io.swagger.oas.inflector.models.RequestContext;
import io.swagger.oas.inflector.processors.BinaryProcessor;
import io.swagger.v3.oas.models.Operation;
import org.testng.annotations.Test;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.Collections;
import java.util.Map;

import static java.io.File.separatorChar;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

public class OpenAPIOperationControllerTest {

    @Test
    public void testFilenameExtraction() throws Exception {

        Map<String, String> headers = Maps.newConcurrentMap();
        assertNull( BinaryProcessor.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "" );
        assertNull( BinaryProcessor.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "  " );
        assertNull( BinaryProcessor.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "  " + separatorChar );
        assertNull( BinaryProcessor.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "test.dat" );
        assertEquals( "test.dat", BinaryProcessor.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "   test.dat  " );
        assertEquals( "test.dat", BinaryProcessor.extractFilenameFromHeaders( headers ));

        headers.put( "filename", separatorChar + "test" + separatorChar + "test.dat" );
        assertEquals( "test.dat", BinaryProcessor.extractFilenameFromHeaders( headers ));

        headers.put( "filename", separatorChar + "test.dat" + separatorChar + separatorChar + separatorChar );
        assertNull( BinaryProcessor.extractFilenameFromHeaders( headers ));
    }

    @Test
    public void testAddsRequestAndResponseToRequestContext() throws Exception {
        final String remoteAddr = "10.11.12.13";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(remoteAddr);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Provider<HttpServletRequest> requestProvider = () -> request;
        Provider<HttpServletResponse> responseProvider = () -> response;
        OpenAPIOperationController controller = new OpenAPIOperationController(mock(Configuration.class), "/any_path",
                "GET", mock(Operation.class), "application/json", Collections.emptyMap(),
                requestProvider, responseProvider
                );
        ContainerRequestContext context = mock(ContainerRequestContext.class);

        RequestContext requestContext = controller.createContext(context);

        assertSame(requestContext.getRequest(), request);
        assertSame(requestContext.getResponse(), response);
        assertEquals(requestContext.getRemoteAddr(), remoteAddr);
    }
}
