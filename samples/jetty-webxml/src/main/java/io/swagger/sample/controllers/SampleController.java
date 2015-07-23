package io.swagger.sample.controllers;

import io.swagger.sample.models.Pet;

import io.swagger.inflector.models.ApiError;
import io.swagger.sample.models.User;
import io.swagger.util.Json;

import java.util.List;

import javax.ws.rs.core.Response;

public class SampleController {
  public Pet addPet(Pet body) {
    return body;
  }
}
