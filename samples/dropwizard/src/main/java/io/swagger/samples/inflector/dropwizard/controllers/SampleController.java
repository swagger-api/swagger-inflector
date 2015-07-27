package io.swagger.samples.inflector.dropwizard.controllers;

import io.swagger.inflector.models.RequestContext;

import io.swagger.inflector.models.ResponseContext;
import io.swagger.samples.inflector.dropwizard.models.Pet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

public class SampleController {
  public ResponseContext addPet(RequestContext request, Pet body) {
    return new ResponseContext()
      .status(Status.OK)
      .entity(body);
  }
  
  public ResponseContext uploadFile(RequestContext request, Long petId, String additionalMetadata, java.io.InputStream file) {
    ByteArrayOutputStream outputStream;
    try {
      outputStream = new ByteArrayOutputStream();
      IOUtils.copy(file, outputStream);
      outputStream.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }
}
