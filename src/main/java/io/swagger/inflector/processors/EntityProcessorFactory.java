package io.swagger.inflector.processors;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

public class EntityProcessorFactory {
  private static List<EntityProcessor> PROCESSORS = new ArrayList<EntityProcessor>();

  static {
    // handles yaml, json, xml
    PROCESSORS.add(new JacksonProcessor());
  }

  public static void addProcessor(EntityProcessor processor) {
    PROCESSORS.add(processor);
  }

  public static Object readValue(MediaType mediaType, InputStream entityStream, Class<?> class1) {
    for(EntityProcessor p : getProcessors()) {
      if(p.supports(mediaType)) {
        return p.process(mediaType, entityStream, class1);
      }
    }
    return null;
  }

  public static List<EntityProcessor> getProcessors() {
    return PROCESSORS;
  }
}
