package io.swagger.test.processors;

import static org.testng.Assert.assertEquals;
import io.swagger.inflector.processors.EntityProcessorFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonProcessorTest {
  @Test
  public void testConvertXMLContent() throws Exception {
    String input = "<user><id>1</id><name>fehguy</name></user>";

    InputStream is = new ByteArrayInputStream(input.getBytes());
    ObjectNode o = (ObjectNode)EntityProcessorFactory.readValue(MediaType.APPLICATION_XML_TYPE, is, JsonNode.class);
    assertEquals(o.getClass(), ObjectNode.class);
    assertEquals(o.get("name").asText(), "fehguy");
  }

  @Test
  public void testConvertJsonContent() throws Exception {
    String input = "{\"name\":\"fehguy\"}";

    InputStream is = new ByteArrayInputStream(input.getBytes());
    ObjectNode o = (ObjectNode)EntityProcessorFactory.readValue(MediaType.APPLICATION_JSON_TYPE, is, JsonNode.class);
    assertEquals(o.getClass(), ObjectNode.class);
    assertEquals(o.get("name").asText(), "fehguy");
  }
  

  @Test
  public void testConvertYamlContent() throws Exception {
    String input = "name: fehguy\nuserId: 42";

    InputStream is = new ByteArrayInputStream(input.getBytes());
    MediaType t = MediaType.valueOf("application/yaml");

    ObjectNode o = (ObjectNode)EntityProcessorFactory.readValue(t, is, JsonNode.class);
    assertEquals(o.getClass(), ObjectNode.class);
    assertEquals(o.get("name").asText(), "fehguy");
    assertEquals(o.get("userId").asText(), "42");
  }
}
