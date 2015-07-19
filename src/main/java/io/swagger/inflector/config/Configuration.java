package io.swagger.inflector.config;

import java.util.HashMap;
import java.util.Map;

public class Configuration {
  final Map<String, Class<?>> modelMap = new HashMap<String, Class<?>>();
  private String controllerPackage = "io.swagger.sample";
  private String modelPackage;

  public void setControllerPackage(String controllerPackage) {
    this.controllerPackage = controllerPackage;
  }
  public String getControllerPackage() {
    return controllerPackage;
  }

  public void setModelPackage(String modelPackage) {
    this.modelPackage = modelPackage;
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