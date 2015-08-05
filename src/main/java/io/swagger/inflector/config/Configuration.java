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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.swagger.util.Yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private final Map<String, Class<?>> modelMap = new HashMap<String, Class<?>>();
    private String controllerPackage;
    private String modelPackage;
    private String swaggerUrl;
    private int invalidRequestCode;
    private String rootPath = "";

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
            System.out.println(is);
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
        return Yaml.mapper().readValue(new File(configLocation), Configuration.class);
    }

    private static Configuration defaultConfiguration() {
        return new Configuration()
                .controllerPackage("io.swagger.sample.controllers")
                .modelPackage("io.swagger.sample.models")
                .swaggerUrl("swagger.yaml");
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

    public Configuration swaggerUrl(String swaggerUrl) {
        this.swaggerUrl = swaggerUrl;
        return this;
    }

    public Configuration() {
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
                cls = Class.forName(className);
                modelMap.put(key, cls);
            } catch (ClassNotFoundException e) {
                LOGGER.error("unable to add mapping for `" + key + "` : `" + className + "`");
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

}