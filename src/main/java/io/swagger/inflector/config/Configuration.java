package io.swagger.inflector.config;

import io.swagger.util.Json;

import io.swagger.models.*;

import io.swagger.sample.User;

import java.util.HashMap;
import java.util.Map;

public class Configuration {
  final Map<String, Class<?>> modelMap = new HashMap<String, Class<?>>();
  private String controllerPackage;
  private String modelPackage;

  public Configuration() {
    controllerPackage = "io.swagger.sample.controllers";
    modelPackage = "io.swagger.sample.models";
    modelMap.put("User", User.class);
  }

  public String getControllerPackage() {
    return controllerPackage;
  }

  public String getModelPackage() {
    return modelPackage;
  }

  public void addModelMapping(String name, Class<?> cls) {
    modelMap.put(name, cls);
  }
  public Class<?> getModelMapping(String name) {
    // TODO: configurable overrides
    return modelMap.get(name);
  }
}