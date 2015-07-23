package io.swagger.sample.controllers;

import io.swagger.sample.models.Pet;
import io.swagger.inflector.models.ApiError;
import io.swagger.inflector.models.RequestWrapper;
import io.swagger.inflector.models.ResponseWrapper;
import io.swagger.sample.models.User;
import io.swagger.util.Json;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class SampleController {
  public ResponseWrapper addPet(RequestWrapper request, Pet body) {
    return new ResponseWrapper()
      .status(Status.OK)
      .entity(body);
  }
}
