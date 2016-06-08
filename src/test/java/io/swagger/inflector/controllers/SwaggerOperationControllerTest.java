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

package io.swagger.inflector.controllers;

import com.google.common.collect.Maps;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class SwaggerOperationControllerTest {

    @Test
    public void testFilenameExtraction() throws Exception {

        Map<String, String> headers = Maps.newConcurrentMap();
        assertNull( SwaggerOperationController.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "" );
        assertNull( SwaggerOperationController.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "  " );
        assertNull( SwaggerOperationController.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "  /" );
        assertNull( SwaggerOperationController.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "test.dat" );
        assertEquals( "test.dat", SwaggerOperationController.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "   test.dat  " );
        assertEquals( "test.dat", SwaggerOperationController.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "/test/test.dat" );
        assertEquals( "test.dat", SwaggerOperationController.extractFilenameFromHeaders( headers ));

        headers.put( "filename", "/test.dat///" );
        assertNull( SwaggerOperationController.extractFilenameFromHeaders( headers ));
    }
}
