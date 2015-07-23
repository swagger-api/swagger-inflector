package io.swagger.test.processors;

import io.swagger.inflector.processors.EntityProcessorFactory;
import io.swagger.util.Json;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import static org.testng.Assert.assertEquals;

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
}
