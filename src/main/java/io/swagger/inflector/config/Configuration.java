/*
 *  Copyright 2016 SmartBear Software
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.inflector.converters.InputConverter;
import io.swagger.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private final Map<String, Class<?>> modelMap = new HashMap<String, Class<?>>();
    private Set<Class<?>> exceptionMappers = new HashSet<Class<?>>();
    private String controllerPackage;
    private String controllerClass;
    private String modelPackage;
    private String swaggerUrl;
    private List<String> swaggerProcessors = new ArrayList<>();
    private String filterClass;
    private int invalidRequestCode = 400;
    private String rootPath = "";
    private Environment environment = Environment.DEVELOPMENT;
    private Set<String> unimplementedModels = new HashSet<String>();
    private List<String> inputConverters = new ArrayList<String>();
    private List<String> inputValidators = new ArrayList<String>();
    private List<String> entityProcessors = new ArrayList<String>();
    private ControllerFactory controllerFactory = new DefaultControllerFactory();
    private String swaggerBase = "/";
    private Set<Direction> validatePayloads = Collections.emptySet();

    public String getSwaggerBase() {
        if("".equals(swaggerBase) || "/".equals(swaggerBase)) {
            return swaggerBase;
        }

        if(swaggerBase != null) {
            if(swaggerBase.endsWith("/")) {
                return swaggerBase.substring(0, swaggerBase.length() - 1);
            }
        }
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

    public enum Direction {
        IN, OUT;
    }

    public static Configuration read() {
        String configLocation = System.getProperty("config", "inflector.yaml");
        System.out.println("loading inflector config from " + configLocation);
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
            URL url = Configuration.class.getClassLoader().getResource("inflector.yaml");
            if(url != null) {
                try {
                    Configuration config = Yaml.mapper().readValue(new File(url.getFile()), Configuration.class);
                    return config;
                } catch (Exception e) {
                  LOGGER.warn("couldn't read inflector config from resource stream");
                  // continue
                }
            }
        } catch (Exception e) {
          LOGGER.warn("Returning default configuration!");
        }
        // try to read from default location, inflector.yaml
        configLocation = "inflector.yaml";
        try {
            return read(configLocation);
        }
        catch (Exception e) {
            // continue
            LOGGER.warn("couldn't read inflector config from system property");
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

    public Configuration filterClass(String filterClass) {
        this.filterClass = filterClass;
        return this;
    }

    public Configuration modelMap(String name, Class<?> cls) {
        modelMap.put(name, cls);
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

    public void setControllerPackage(String controllerPackage) {
        this.controllerPackage = controllerPackage;
    }

    public String getControllerPackage() {
        return controllerPackage;
    }

    public String getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }

    public String getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
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
        if(System.getProperty("swaggerUrl") != null) {
            return System.getProperty("swaggerUrl");
        }
        return swaggerUrl;
    }
    public void setSwaggerUrl(String swaggerUrl) {
        this.swaggerUrl = swaggerUrl;
    }

    public List<String> getSwaggerProcessors() {
        return swaggerProcessors;
    }

    public void setSwaggerProcessors(List<String> swaggerProcessors) {
        this.swaggerProcessors = swaggerProcessors;
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

    public Set<Direction> getValidatePayloads() {
        return validatePayloads;
    }

    @JsonDeserialize(using = DirectionDeserializer.class)
    public void setValidatePayloads(Set<Direction> validatePayloads) {
        this.validatePayloads = validatePayloads;
    }

    public String getControllerFactoryClass() {
        return controllerFactory.getClass().getName();
    }

	public void setControllerFactoryClass(String controllerFactoryClass) {
        if (!StringUtils.isEmpty(controllerFactoryClass)) {
            try {
                controllerFactory = Class.forName(controllerFactoryClass).asSubclass(ControllerFactory.class)
                        .newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOGGER.error("Couldn't create controller factory", e);
            }
        }
    }

    public void setControllerFactory(ControllerFactory controllerFactory) {
        this.controllerFactory = controllerFactory;
    }
}
