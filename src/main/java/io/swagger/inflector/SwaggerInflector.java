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

package io.swagger.inflector;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;
import io.swagger.config.FilterFactory;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.controllers.InflectResultController;
import io.swagger.inflector.controllers.SwaggerOperationController;
import io.swagger.inflector.controllers.SwaggerResourceController;
import io.swagger.inflector.converters.Converter;
import io.swagger.inflector.converters.InputConverter;
import io.swagger.inflector.models.InflectResult;
import io.swagger.inflector.processors.*;
import io.swagger.inflector.utils.DefaultSpecFilter;
import io.swagger.inflector.utils.ResolverUtil;
import io.swagger.inflector.validators.Validator;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.*;

public class SwaggerInflector extends ResourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerInflector.class);
    private Configuration config;
    private String basePath;
    private String originalBasePath;
    private ServletContext servletContext;
    private Map<String, List<String>> missingOperations = new HashMap<String, List<String>>();
    private Set<String> unimplementedMappedModels = new TreeSet<String>();

    public SwaggerInflector(Configuration configuration) {
        init(configuration);
    }

    public SwaggerInflector(@Context ServletContext ctx) {
        this.servletContext = ctx;
        Configuration config = null;
        if (servletContext != null) {
            if (servletContext.getInitParameter("inflector-config") != null) {
                try {
                    config = Configuration.read(servletContext.getInitParameter("inflector-config"));
                } catch (Exception e) {
                    LOGGER.error("unable to read configuration from init param");
                }
            }
        }
        if (config == null) {
            // use default location
            config = Configuration.read();
        }
        init(config);
    }

    protected void init(Configuration configuration) {
        config = configuration;
        SwaggerDeserializationResult swaggerParseResult = new SwaggerParser().readWithInfo(config.getSwaggerUrl(), null, true);
        Swagger swagger = swaggerParseResult.getSwagger();

        if(config.isValidatePayloads()) {
            LOGGER.info("resolving swagger");
            new ResolverUtil().resolveFully(swagger);
        }

        if (swagger != null) {
            originalBasePath = swagger.getBasePath();
            StringBuilder b = new StringBuilder();

            if (!"".equals(configuration.getRootPath()))
                b.append(configuration.getRootPath());
            if (swagger.getBasePath() != null) {
                b.append(swagger.getBasePath());
            }
            if (b.length() > 0) {
                swagger.setBasePath(b.toString());
            }

            Map<String, Path> paths = swagger.getPaths();
            Map<String, Model> definitions = swagger.getDefinitions();
            for (String pathString : paths.keySet()) {
                Path path = paths.get(pathString);
                final Resource.Builder builder = Resource.builder();
                this.basePath = configuration.getRootPath() + swagger.getBasePath();

                builder.path(basePath(originalBasePath, pathString));
                Operation operation;

                operation = path.getGet();
                if (operation != null) {
                    addOperation(pathString, builder, HttpMethod.GET, operation, definitions);
                }
                operation = path.getPost();
                if (operation != null) {
                    addOperation(pathString, builder, HttpMethod.POST, operation, definitions);
                }
                operation = path.getPut();
                if (operation != null) {
                    addOperation(pathString, builder, HttpMethod.PUT, operation, definitions);
                }
                operation = path.getDelete();
                if (operation != null) {
                    addOperation(pathString, builder, HttpMethod.DELETE, operation, definitions);
                }
                operation = path.getOptions();
                if (operation != null) {
                    addOperation(pathString, builder, HttpMethod.OPTIONS, operation, definitions);
                }
                operation = path.getPatch();
                if (operation != null) {
                    addOperation(pathString, builder, "PATCH", operation, definitions);
                }
                registerResources(builder.build());
            }
        } else {
            LOGGER.error("No swagger definition detected!  Not much to do...");
        }
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonNodeExampleSerializer());

        // filters
        if (config.getFilterClass() != null) {
            if(!config.getFilterClass().isEmpty()) {
                try {
                    FilterFactory.setFilter((SwaggerSpecFilter) SwaggerInflector.class.getClassLoader().loadClass(config.getFilterClass()).newInstance());
                }
                catch (Exception e) {
                    LOGGER.error("Unable to set filter class " + config.getFilterClass());
                }
            }
        }
        else {
            FilterFactory.setFilter(new DefaultSpecFilter());
        }

        if(swagger == null) {
            LOGGER.error("the swagger definition is not valid");
        }

        // JSON
        if (config.getEntityProcessors().contains("json")) {
            Json.mapper().registerModule(simpleModule);
            register(JacksonJsonProvider.class);
            register(JsonExampleProvider.class);
            register(JsonProvider.class);
            enableSwaggerJSON(swagger);
        }

        // XML
        if (config.getEntityProcessors().contains("xml")) {
            register(JacksonJaxbXMLProvider.class);
            register(XMLExampleProvider.class);
        }

        // YAML
        if (config.getEntityProcessors().contains("yaml")) {
            Yaml.mapper().registerModule(simpleModule);
            register(YamlExampleProvider.class);
            enableSwaggerYAML(swagger);
        }

        register(new MultiPartFeature());

        // Swagger serializers
        register(SwaggerSerializers.class);

        for (Class<?> exceptionMapper : config.getExceptionMappers()) {
            register(exceptionMapper);
        }

        // validators
        if (config.getInputValidators() != null && config.getInputValidators().size() > 0) {
            InputConverter.getInstance().getValidators().clear();
            for (String inputValidator : config.getInputValidators()) {
                try {
                    String clsName = inputValidator;
                    if ("requiredFieldValidator".equalsIgnoreCase(inputValidator)) {
                        clsName = "io.swagger.inflector.validators.DefaultValidator";
                    }
                    if ("numericValidator".equalsIgnoreCase(inputValidator)) {
                        clsName = "io.swagger.inflector.validators.NumericValidator";
                    }
                    if ("stringValidator".equalsIgnoreCase(inputValidator)) {
                        clsName = "io.swagger.inflector.validators.StringTypeValidator";
                    }
                    InputConverter.getInstance().addValidator((Validator) Class.forName(clsName).newInstance());
                } catch (Exception e) {
                    LOGGER.warn("unable to add validator `" + inputValidator + "`");
                    e.printStackTrace();
                }
            }
        } else {
            InputConverter.getInstance().defaultValidators();
        }

        // converters
        if (config.getInputConverters() != null && config.getInputConverters().size() > 0) {
            InputConverter.getInstance().getConverters().clear();
            for (String converter : config.getInputConverters()) {
                try {
                    String clsName = converter;
                    if ("defaultConverter".equalsIgnoreCase(converter)) {
                        clsName = "io.swagger.inflector.converters.DefaultConverter";
                    }
                    LOGGER.debug("adding converter `" + clsName + "`");
                    InputConverter.getInstance().addConverter((Converter) Class.forName(clsName).newInstance());
                } catch (Exception e) {
                    LOGGER.warn("unable to add validator `" + converter + "`");
                }
            }
        } else {
            InputConverter.getInstance().defaultConverters();
        }

        InflectResult result = new InflectResult();
        for(String key: swaggerParseResult.getMessages()) {
            result.specParseMessage(key);
        }
        for(String key: missingOperations.keySet()) {
            result.unimplementedControllers(key, missingOperations.get(key));
        }
        for(String model: config.getUnimplementedModels()) {
            result.unimplementedModel(model);
        }
        for(String model: unimplementedMappedModels) {
            result.unimplementedModel(model);
        }

        if (Configuration.Environment.DEVELOPMENT.equals(configuration.getEnvironment())) {
            if(missingOperations.size() > 0) {
                LOGGER.debug("There are unimplemented operations!");
            }
            for(String key: missingOperations.keySet()) {
                LOGGER.debug(key);
                for(String val: missingOperations.get(key)) {
                    LOGGER.debug(" - " + val);
                }
            }
            final Resource.Builder builder = Resource.builder();
            builder.path(basePath(originalBasePath, config.getSwaggerBase() + "debug.json"))
                    .addMethod(HttpMethod.GET)
                    .produces(MediaType.APPLICATION_JSON)
                    .handledBy(new InflectResultController(result))
                    .build();

            registerResources(builder.build());
        }
        else if (Configuration.Environment.STAGING.equals(configuration.getEnvironment())) {
            if(missingOperations.size() > 0) {
                LOGGER.warn("There are unimplemented operations!");
            }
            for(String key: missingOperations.keySet()) {
                LOGGER.warn(key);
                for(String val: missingOperations.get(key)) {
                    LOGGER.warn(" - " + val);
                }
            }
        }
        else if (Configuration.Environment.PRODUCTION.equals(configuration.getEnvironment())) {
            if(missingOperations.size() > 0) {
                LOGGER.error("There are unimplemented operations!");
            }
            for(String key: missingOperations.keySet()) {
                LOGGER.error(key);
                for(String val: missingOperations.get(key)) {
                    LOGGER.error(" - " + val);
                }
            }
            if(missingOperations.size() > 0) {
                LOGGER.error("Unable to start due to unimplemented methods");
                throw new RuntimeException("Unable to start due to unimplemented methods");
            }
        }
    }

    private String basePath(String basePath, String path) {
        if (StringUtils.isEmpty(basePath) || "/".equals(basePath)) {
            return path;
        }
        return basePath + path;
    }

    private void enableSwaggerJSON(Swagger swagger) {
        final Resource.Builder builder = Resource.builder();
        builder.path(basePath(originalBasePath, config.getSwaggerBase() + "swagger.json"))
                .addMethod(HttpMethod.GET)
                .produces(MediaType.APPLICATION_JSON)
                .handledBy(new SwaggerResourceController(swagger))
                .build();

        registerResources(builder.build());
    }

    private void enableSwaggerYAML(Swagger swagger) {
        final Resource.Builder builder = Resource.builder();
        builder.path(basePath(originalBasePath, config.getSwaggerBase() + "swagger.yaml"))
                .addMethod(HttpMethod.GET)
                .produces("application/yaml")
                .handledBy(new SwaggerResourceController(swagger))
                .build();

        registerResources(builder.build());
    }

    private void addOperation(String pathString, Resource.Builder builder, String method, Operation operation, Map<String, Model> definitions) {
        LOGGER.debug("adding operation for `" + pathString + "` " + method);
        SwaggerOperationController controller = new SwaggerOperationController(config, pathString, method, operation, definitions);
        if (controller.getMethod() == null) {
            if (controller.getMethodName() != null) {
                List<String> missingMethods = missingOperations.get(controller.getControllerName());
                if (missingMethods == null) {
                    missingMethods = new ArrayList<String>();
                    missingOperations.put(controller.getControllerName(), missingMethods);
                }
                missingMethods.add(controller.getOperationSignature());
            }
        }
        unimplementedMappedModels.addAll(controller.getUnimplementedMappedModels());
        builder.addMethod(method).handledBy(controller);
    }
}
