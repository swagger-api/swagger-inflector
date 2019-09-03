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

package io.swagger.oas.test.processors;


import io.swagger.oas.inflector.config.Configuration;
import io.swagger.oas.inflector.controllers.OpenAPIOperationController;
import io.swagger.oas.inflector.processors.BinaryProcessor;
import io.swagger.oas.inflector.processors.EntityProcessor;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import mockit.Mocked;

import mockit.StrictExpectations;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BinaryProcessorTest {

    private static final MediaType BINARY_TYPE = MediaType.APPLICATION_OCTET_STREAM_TYPE;
    private final EntityProcessor processor = new BinaryProcessor();

    @Test
    public void supportsTest() {
        assertTrue(processor.supports(BINARY_TYPE));

        MediaType zipMediaType = new MediaType("application", "zip");
        processor.enableType(zipMediaType);
        assertTrue( processor.supports( zipMediaType ));
    }

    @Test
    public void processTestWithException() throws UnsupportedOperationException {
        final byte[] expected = "binary string".getBytes();
        try {
            final byte[] actual = (byte[]) processor.process(MediaType.APPLICATION_OCTET_STREAM_TYPE,
                    new ByteArrayInputStream(expected), byte[].class);
            Assert.fail("No exception was thrown");
            assertEquals(actual, expected);
        }catch (Exception e){

        }
    }

    @Test
    public void processTest(@Mocked Configuration config, @Mocked Map<String,Schema> definitions){

        Operation operation = new Operation().requestBody(new RequestBody().content(new Content().
                addMediaType( "application/octec-stream",
                        new io.swagger.v3.oas.models.media.MediaType().
                                schema(new Schema()
                                        .type("string")
                                        .format("binary")))));

        OpenAPIOperationController controller = new OpenAPIOperationController(config,"/primitiveBody/binary", HttpMethod.POST, operation,"application/octec-stream", definitions);

        final byte[] expected = "binary string".getBytes();

        try {
            final byte[] actual = (byte[]) processor.process(MediaType.APPLICATION_OCTET_STREAM_TYPE,
                    new ByteArrayInputStream(expected), byte[].class,controller);

            assertEquals(actual, expected);
        }catch (Exception e){


        }

    }

}
