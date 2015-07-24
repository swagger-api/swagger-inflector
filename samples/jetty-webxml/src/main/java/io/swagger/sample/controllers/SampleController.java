package io.swagger.sample.controllers;

import io.swagger.inflector.models.RequestWrapper;

import io.swagger.inflector.models.ResponseWrapper;
import io.swagger.sample.models.Pet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

public class SampleController {
  public ResponseWrapper addPet(RequestWrapper request, Pet body) {
    return new ResponseWrapper()
      .status(Status.OK)
      .entity(body);
  }
  
  public ResponseWrapper uploadFile(io.swagger.inflector.models.RequestWrapper request, java.lang.Long petId, java.lang.String additionalMetadata, java.io.InputStream file) {    
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
