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

package io.swagger.test.processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.inflector.processors.EntityProcessorFactory;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;

public class JacksonProcessorTest {
    @Test
    public void testConvertXMLContent() throws Exception {
        String input = "<user><id>1</id><name>fehguy</name></user>";

        InputStream is = new ByteArrayInputStream(input.getBytes());
        ObjectNode o = (ObjectNode) EntityProcessorFactory.readValue(MediaType.APPLICATION_XML_TYPE, is, JsonNode.class);
        assertEquals(o.getClass(), ObjectNode.class);
        assertEquals(o.get("name").asText(), "fehguy");
    }

    @Test
    public void testConvertJsonContent() throws Exception {
        String input = "{\"name\":\"fehguy\"}";

        InputStream is = new ByteArrayInputStream(input.getBytes());
        ObjectNode o = (ObjectNode) EntityProcessorFactory.readValue(MediaType.APPLICATION_JSON_TYPE, is, JsonNode.class);
        assertEquals(o.getClass(), ObjectNode.class);
        assertEquals(o.get("name").asText(), "fehguy");
    }

    @Test
    public void testConvertYamlContent() throws Exception {
        String input = "name: fehguy\nuserId: 42";

        InputStream is = new ByteArrayInputStream(input.getBytes());
        MediaType t = MediaType.valueOf("application/yaml");

        ObjectNode o = (ObjectNode) EntityProcessorFactory.readValue(t, is, JsonNode.class);
        assertEquals(o.getClass(), ObjectNode.class);
        assertEquals(o.get("name").asText(), "fehguy");
        assertEquals(o.get("userId").asText(), "42");
    }

    @Test
    public void testConvertYamlWithEncoding() throws Exception {
        final String input = "type: application\nsubtype: yaml\ncharset: UTF-8";
        final MediaType type = new MediaType("application", "yaml", StandardCharsets.UTF_8.name());

        final String string = (String) EntityProcessorFactory.readValue(type,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), String.class);
        assertEquals(string, input);

        final ObjectNode json = (ObjectNode) EntityProcessorFactory.readValue(type,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), JsonNode.class);
        assertEquals(json.get("type").asText(), "application");
        assertEquals(json.get("subtype").asText(), "yaml");
        assertEquals(json.get("charset").asText(), "UTF-8");
    }
}
