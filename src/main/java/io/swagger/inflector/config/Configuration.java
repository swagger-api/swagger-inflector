/*
 *  Copyright 2015 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.swagger.inflector.config;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.inflector.converters.InputConverter;
import io.swagger.util.Yaml;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private final Map<String, Class<?>> modelMap = new HashMap<String, Class<?>>();
    private Set<Class<?>> exceptionMappers = new HashSet<Class<?>>();
    private String controllerPackage;
    private String modelPackage;
    private String swaggerUrl;
    private int invalidRequestCode = 400;
    private String rootPath = "";
    private Environment environment = Environment.DEVELOPMENT;
    private Set<String> unimplementedModels = new HashSet<String>();
    private List<String> inputConverters = new ArrayList<String>();
    private List<String> inputValidators = new ArrayList<String>();
    private List<String> entityProcessors = new ArrayList<String>();
    private ControllerFactory controllerFactory = new DefaultControllerFactory();
    private String swaggerBase = "/";

    public String getSwaggerBase() {
        return swaggerBase;
    }

    public static enum Environment {
        DEVELOPMENT(1, "development"), STAGING(2, "staging"), PRODUCTION(3, "production");

        private Integer id;
        private String name;

        private Environment(final Integer id, final String name) {
            this.id = id;
            this.name = name;
        }

        @JsonValue
        public String getName() {
            return name;
        }
    }

    public static Configuration read() {
        String configLocation = System.getProperty("config", "inflector.yaml");
        System.out.println("loading config from " + configLocation);
        if(configLocation != null) {
          try {
            return read(configLocation);
          }
          catch (Exception e) {
            // continue
            LOGGER.warn("couldn't read inflector config from system property");
          }
        }
        try {
            // try to load from resources
            
            InputStream is = Configuration.class.getClassLoader().getResourceAsStream("/WEB-INF/inflector.yaml");
            if(is != null) {
                try {
                  return Yaml.mapper().readValue(is, Configuration.class);
                } catch (Exception e) {
                  LOGGER.warn("couldn't read inflector config from resource stream");
                  // continue
                }
            }
        } catch (Exception e) {
          LOGGER.warn("Returning default configuration!");
        }
        return defaultConfiguration();
    }

    public static Configuration read(String configLocation) throws Exception {
        Configuration config = Yaml.mapper().readValue(new File(configLocation), Configuration.class);
        if(config != null && config.getExceptionMappers().size() == 0) {
          config.setExceptionMappers(Configuration.defaultConfiguration().getExceptionMappers());
        }
        String environment = System.getProperty("environment");
        if(environment != null) {
            System.out.println("Overriding environment to " + environment);
            config.setEnvironment(Environment.valueOf(environment));
        }
        return config;
    }

    public static Configuration defaultConfiguration() {
        return new Configuration()
            .controllerPackage("io.swagger.sample.controllers")
            .modelPackage("io.swagger.sample.models")
            .swaggerUrl("swagger.yaml")
            .exceptionMapper("io.swagger.inflector.utils.DefaultExceptionMapper")
            .defaultValidators()
            .defaultConverters()
            .defaultProcessors();
    }

    public Configuration defaultValidators() {
        InputConverter.getInstance().defaultValidators();
        return this;
    }

    public Configuration defaultConverters() {
        InputConverter.getInstance().defaultConverters();
        return this;
    }

    public Configuration defaultProcessors() {
        InputConverter.getInstance().defaultValidators();
        return this;
  }
    
    public Configuration modelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
        return this;
    }

    public Configuration controllerPackage(String controllerPackage) {
        this.controllerPackage = controllerPackage;
        return this;
    }

    public Configuration modelMap(String name, Class<?> cls) {
        modelMap.put(name, cls);
        return this;
    }

    public Configuration controllerInstantiator( ControllerFactory instantiator ){
        this.controllerFactory = instantiator;
        return this;
    }

    public Configuration swaggerUrl(String swaggerUrl) {
        this.swaggerUrl = swaggerUrl;
        return this;
    }
    
    public Configuration exceptionMapper(String className) {
      Class<?> cls;
      try {
          ClassLoader classLoader = Configuration.class.getClassLoader();
          cls = classLoader.loadClass(className);
          exceptionMappers.add(cls);
      } catch (ClassNotFoundException e) {
          LOGGER.error("unable to add exception mapper for `" + className + "`, " + e.getMessage());
      }
      return this;
    }

    public Configuration() {
    }

    public ControllerFactory getControllerFactory() {
        return controllerFactory;
    }

    public void setControllerFactory(ControllerFactory controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

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

    public void setModelMappings(Map<String, String> mappings) {
        for (String key : mappings.keySet()) {
            String className = mappings.get(key);
            Class<?> cls;
            try {
                ClassLoader classLoader = Configuration.class.getClassLoader();
                cls = classLoader.loadClass(className);
                modelMap.put(key, cls);
            } catch (ClassNotFoundException e) {
                unimplementedModels.add(className);
                LOGGER.error("unable to add mapping for `" + key + "` : `" + className + "`, " + e.getMessage());
            }
        }
    }

    public Map<String, String> getModelMappings() {
        Map<String, String> output = new HashMap<String, String>();
        for (String key : modelMap.keySet()) {
            Class<?> value = modelMap.get(key);
            output.put(key, value.getCanonicalName());
        }
        return output;
    }

    @JsonIgnore
    public void addModelMapping(String name, Class<?> cls) {
        modelMap.put(name, cls);
    }
    public Class<?> getModelMapping(String name) {
        return modelMap.get(name);
    }

    public String getSwaggerUrl() {
        return swaggerUrl;
    }
    public void setSwaggerUrl(String swaggerUrl) {
        this.swaggerUrl = swaggerUrl;
    }

    public void setInvalidRequestStatusCode(int code) {
        this.invalidRequestCode = code;
    }
    public int getInvalidRequestStatusCode() {
        return invalidRequestCode;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
    public String getRootPath() {
        return rootPath;
    }

    public Set<Class<?>> getExceptionMappers() {
        return exceptionMappers;
    }
    public void setExceptionMappers(Set<Class<?>> exceptionMappers) {
        this.exceptionMappers = exceptionMappers;
    }

    public List<String> getEntityProcessors() {
        return entityProcessors;
    }
    public void setEntityProcessors(List<String> entityProcessors) {
        this.entityProcessors = entityProcessors;
    }

    public List<String> getInputValidators() {
        return inputValidators;
    }
    public void setInputValidators(List<String> inputValidators) {
        this.inputValidators = inputValidators;
    }

    public List<String> getInputConverters() {
        return inputConverters;
    }
    public void setInputConverters(List<String> inputConverters) {
        this.inputConverters = inputConverters;
    }

    public Environment getEnvironment() {
        return environment;
    }
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Set<String> getUnimplementedModels() {
        return unimplementedModels;
    }

    public void setUnimplementedModels(Set<String> unimplementedModels) {
        this.unimplementedModels = unimplementedModels;
    }

    public void setSwaggerBase(String swaggerBase) {
        this.swaggerBase = swaggerBase;
    }
}