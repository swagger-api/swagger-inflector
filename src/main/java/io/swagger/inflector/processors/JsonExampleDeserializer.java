package io.swagger.inflector.processors;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.examples.models.ObjectExample;
import io.swagger.inflector.examples.models.StringExample;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class JsonExampleDeserializer extends JsonDeserializer<Example> {

  @Override
  public Example deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    Example output = null;
    JsonNode node = jp.getCodec().readTree(jp);
    if(node instanceof ObjectNode) {
      ObjectExample obj = new ObjectExample();
      ObjectNode on = (ObjectNode) node;
      
      for(Iterator<Entry<String, JsonNode>> x = on.fields(); x.hasNext();) {
        Entry<String, JsonNode> i = x.next();
        String key = i.getKey();
        JsonNode value = i.getValue();
        
        obj.put(key, new StringExample(value.asText()));
        output = obj;
      }
    }
    else if(node instanceof TextNode) {
      output = new StringExample(((TextNode)node).asText());
    }
    return output;
  }
}
