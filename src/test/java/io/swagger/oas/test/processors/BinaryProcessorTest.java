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

import io.swagger.oas.inflector.converters.ConversionException;
import io.swagger.oas.inflector.processors.BinaryProcessor;
import io.swagger.oas.inflector.processors.EntityProcessor;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;

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
    public void processTest() throws ConversionException {
        final byte[] expected = "binary string".getBytes();
        final byte[] actual = (byte[]) processor.process(MediaType.APPLICATION_OCTET_STREAM_TYPE,
                new ByteArrayInputStream(expected), byte[].class);
        assertEquals(actual, expected);
    }

}
