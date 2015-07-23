package io.swagger.inflector.processors;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;

public interface EntityProcessor {
  boolean supports(MediaType mediaType);

  Object process(MediaType mediaType, InputStream entityStream, Class<?> cls);
}
