package io.swagger.inflector.processors;

import io.swagger.util.Json;

import io.swagger.util.Yaml;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JacksonProcessor implements EntityProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(JacksonProcessor.class);

  private static XmlMapper XML = new XmlMapper();

  @Override
  public boolean supports(MediaType mediaType) {
    if(MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
      return true;
    }
    if(MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)) {
      return true;
    }
    if(mediaType.toString().equalsIgnoreCase("application/yaml")) {
      return true;
    }
    return false;
  }

  @Override
  public Object process(MediaType mediaType, InputStream entityStream, Class<?> cls) {
    try {
      if(MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
        return Json.mapper().readValue(entityStream, cls);
      }
      if(MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)) {
        return XML.readValue(entityStream, cls);
      }
      if(mediaType.toString().equalsIgnoreCase("application/yaml")) {
        return Yaml.mapper().readValue(entityStream, cls);
      }
    } catch (IOException e) {
      LOGGER.error("unable to extract entity from content-type `" + mediaType + "` to " + cls.getCanonicalName(), e);
    }

    return null;
  }
}