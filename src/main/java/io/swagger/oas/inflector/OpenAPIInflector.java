package io.swagger.oas.inflector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;
import io.swagger.oas.inflector.config.ExposedSpecOptions;
import io.swagger.oas.inflector.config.FilterFactory;

import io.swagger.oas.inflector.config.Configuration;
import io.swagger.oas.inflector.controllers.InflectResultController;
import io.swagger.oas.inflector.controllers.OpenAPIOperationController;
import io.swagger.oas.inflector.controllers.OpenAPIResourceController;
import io.swagger.oas.inflector.converters.Converter;
import io.swagger.oas.inflector.converters.InputConverter;
import io.swagger.oas.inflector.models.InflectResult;
import io.swagger.oas.inflector.processors.EntityProcessor;
import io.swagger.oas.inflector.processors.EntityProcessorFactory;
import io.swagger.oas.inflector.processors.JacksonProcessor;
import io.swagger.oas.inflector.processors.PlainProcessor;
import io.swagger.oas.inflector.processors.PlainExampleProvider;
import io.swagger.oas.inflector.processors.JsonExampleProvider;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.oas.inflector.processors.JsonProvider;
import io.swagger.oas.inflector.processors.XMLExampleProvider;
import io.swagger.oas.inflector.processors.YamlExampleProvider;
import io.swagger.oas.inflector.utils.DefaultContentTypeProvider;
import io.swagger.oas.inflector.utils.DefaultSpecFilter;
import io.swagger.oas.inflector.utils.ExtensionsUtil;
import io.swagger.oas.inflector.validators.Validator;
import io.swagger.v3.core.filter.OpenAPISpecFilter;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;


import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.parser.util.ClasspathHelper;
import io.swagger.v3.parser.util.RemoteUrl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.ServletContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class OpenAPIInflector extends ResourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIInflector.class);
    private Configuration config;
    private String basePath;
    private String originalBasePath;
    private ServletContext servletContext;
    private Map<String, List<String>> missingOperations = new HashMap<>();
    private Set<String> unimplementedMappedModels = new TreeSet<>();



    private ObjectMapper objectMapper;

    public OpenAPIInflector(Configuration configuration) {
        this(configuration, Json.mapper());
    }

    public OpenAPIInflector(Configuration configuration,ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
        init(configuration);
    }

    public OpenAPIInflector(@Context ServletContext ctx) {
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
        objectMapper = Json.mapper();
        init(config);
    }
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    protected void init(Configuration configuration) {
        config = configuration;
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);
        SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readLocation(config.getSwaggerUrl(), null, options);
        
        // Dump any warning messages the parser might produce
        if (!swaggerParseResult.getMessages().isEmpty()) {
            for (String message : swaggerParseResult.getMessages()) {
                LOGGER.warn(message);
            }
        }
        
        OpenAPI openAPI = swaggerParseResult.getOpenAPI();

        OpenAPI exposedAPI = getExposedAPI(config);

        if(!config.getValidatePayloads().isEmpty()) {
            LOGGER.info("resolving openAPI");
            new ExtensionsUtil().addExtensions(openAPI);
        }

        if (openAPI != null) {
            String basePath = null;
            List<Server> servers = openAPI.getServers();
            if (servers != null && servers.size() > 0){
                String url = servers.get(0).getUrl();
                if(StringUtils.isNotBlank(servers.get(0).getUrl())) {
                    basePath = url.substring(url.lastIndexOf("/"));
                    originalBasePath = url.substring(url.lastIndexOf("/"));
                }


            }

            StringBuilder b = new StringBuilder();

            if (!"".equals(configuration.getRootPath()))
                b.append(configuration.getRootPath());
            if (basePath != null) {
                b.append(basePath);
            }
            if (b.length() > 0) {
                openAPI.getServers().get(0).setUrl(b.toString());
                if (configuration.getExposedSpecOptions().isMergeRootPath()) {
                    exposedAPI.getServers().get(0).setUrl(b.toString());
                }
            }

            Map<String, PathItem> paths = openAPI.getPaths();
            Map<String, Schema> definitions = null;
            if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) {
                definitions = new HashMap<>();
            } else {
                definitions = openAPI.getComponents().getSchemas();
            }
            for (String pathString : paths.keySet()) {
                PathItem pathItem = paths.get(pathString);
                final Resource.Builder builder = Resource.builder();
                this.basePath = configuration.getRootPath() + openAPI.getServers().get(0).getUrl();

                builder.path(basePath(originalBasePath, pathString));
                Operation operation;

                operation = pathItem.getGet();
                if (operation != null) {
                    addOperation(pathString, builder, HttpMethod.GET, operation, definitions);
                }
                operation = pathItem.getPost();
                if (operation != null) {
                    addOperation(pathString, builder, HttpMethod.POST, operation, definitions);
                }
                operation = pathItem.getPut();
                if (operation != null) {
                    addOperation(pathString, builder, HttpMethod.PUT, operation, definitions);
                }
                operation = pathItem.getDelete();
                if (operation != null) {
                    addOperation(pathString, builder, HttpMethod.DELETE, operation, definitions);
                }
                operation = pathItem.getOptions();
                if (operation != null) {
                    addOperation(pathString, builder, HttpMethod.OPTIONS, operation, definitions);
                }
                operation = pathItem.getPatch();
                if (operation != null) {
                    addOperation(pathString, builder, "PATCH", operation, definitions);
                }
                registerResources(builder.build());
            }
        } else {
            LOGGER.error("No openAPI definition detected!  Not much to do...");
        }
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonNodeExampleSerializer());

        // filters
        if (config.getFilterClass() != null) {
            if(!config.getFilterClass().isEmpty()) {
                try {
                    FilterFactory.setFilter((OpenAPISpecFilter) OpenAPIInflector.class.getClassLoader().loadClass(config.getFilterClass()).newInstance());
                }
                catch (Exception e) {
                    LOGGER.error("Unable to set filter class " + config.getFilterClass());
                }
            }
        }
        else {
           FilterFactory.setFilter(new DefaultSpecFilter());
        }

        if(openAPI == null) {
            LOGGER.error("the openAPI definition is not valid");
        }

        // Add content providers in order or appearance in the configuration
        for (String item : config.getEntityProcessors()) {
            if ("json".equalsIgnoreCase(item)) {
                // JSON
                getObjectMapper().registerModule(simpleModule);
                register(JacksonJsonProvider.class);
                register(JsonExampleProvider.class);

                // If a custom object mapper has this INDENT_OUTPUT specified already,
                // disable the the JsonProvider as it's redundant
                if(!getObjectMapper().isEnabled(SerializationFeature.INDENT_OUTPUT))
                {
                    register(new JsonProvider(config.isPrettyPrint()));
                }
                if (!isRegistered(DefaultContentTypeProvider.class)) {
                    register(new DefaultContentTypeProvider(MediaType.APPLICATION_JSON_TYPE),
                            ContextResolver.class);
                }
                enableProcessor(JacksonProcessor.class, MediaType.APPLICATION_JSON_TYPE);
                enableSwaggerJSON(exposedAPI, configuration.getSwaggerProcessors());
            } else if ("xml".equalsIgnoreCase(item)) {
                // XML
                if (!isRegistered(DefaultContentTypeProvider.class)) {
                    register(new DefaultContentTypeProvider(MediaType.APPLICATION_XML_TYPE),
                            ContextResolver.class);
                }
                enableProcessor(JacksonProcessor.class, MediaType.APPLICATION_XML_TYPE);
                register(JacksonJaxbXMLProvider.class);
                register(XMLExampleProvider.class);
            } else if ("yaml".equalsIgnoreCase(item)) {
                // YAML
                Yaml.mapper().registerModule(simpleModule);
                register(YamlExampleProvider.class);
                enableProcessor(JacksonProcessor.class, JacksonProcessor.APPLICATION_YAML_TYPE);
                enableSwaggerYAML(exposedAPI, configuration.getSwaggerProcessors());
            }else if ("plain".equalsIgnoreCase(item)) {
                // PLAIN
                register(PlainExampleProvider.class);
                enableProcessor(PlainProcessor.class, MediaType.TEXT_PLAIN_TYPE);
            }
        }

        register(new MultiPartFeature());

        // Swagger serializers
        register(SwaggerSerializers.class);
        SwaggerSerializers.setPrettyPrint(config.isPrettyPrint());

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
                        clsName = "io.swagger.oas.inflector.validators.DefaultValidator";
                    }
                    if ("numericValidator".equalsIgnoreCase(inputValidator)) {
                        clsName = "io.swagger.oas.inflector.validators.NumericValidator";
                    }
                    if ("stringValidator".equalsIgnoreCase(inputValidator)) {
                        clsName = "io.swagger.oas.inflector.validators.StringTypeValidator";
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
                        clsName = "io.swagger.oas.inflector.converters.DefaultConverter";
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

    public static String basePath(String basePath, String path) {
        if(StringUtils.isBlank(basePath)) {
            basePath = "/";
        }
        if (!basePath.endsWith("/") && !"/".equals(basePath) && StringUtils.isBlank(path)) {
            basePath = basePath + "/";
        }
        if (StringUtils.isEmpty(path)) {
            return basePath;
        }
        if (path.equals("/")) {
            return basePath + "/";
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if ("/".equals(basePath)) {
            basePath = "";
        }
        if(basePath.endsWith("/") && path.startsWith("/")) {
            path = path.substring(1);
        }
        return basePath + path;
    }

    private void enableProcessor(Class<?> cls, MediaType type) {
        List<EntityProcessor> processors = EntityProcessorFactory.getProcessors();
        for(EntityProcessor processor : processors) {
            if (processor.getClass().equals(cls)) {
                processor.enableType(type);
                return;
            }
        }
        try {
            EntityProcessor processor = (EntityProcessor) cls.newInstance();
            processor.enableType(type);
        }
        catch (Exception e) {
            LOGGER.error("unable to initialize class " + cls);
        }
    }

    private void enableSwaggerJSON(OpenAPI openAPI, List<String> swaggerProcessors) {
        final Resource.Builder builder = Resource.builder();
        builder.path(basePath(originalBasePath, StringUtils.appendIfMissing(config.getSwaggerBase(), "/") + "openapi.json"))
                .addMethod(HttpMethod.GET)
                .produces(MediaType.APPLICATION_JSON)
                .handledBy(new OpenAPIResourceController(openAPI, swaggerProcessors))
                .build();

        registerResources(builder.build());
    }

    private void enableSwaggerYAML(OpenAPI openAPI, List<String> swaggerProcessors) {
        final Resource.Builder builder = Resource.builder();
        builder.path(basePath(originalBasePath, StringUtils.appendIfMissing(config.getSwaggerBase(), "/") + "openapi.yaml"))
                .addMethod(HttpMethod.GET)
                .produces("application/yaml")
                .handledBy(new OpenAPIResourceController(openAPI, swaggerProcessors))
                .build();

        registerResources(builder.build());
    }

    private void addOperation(String pathString, Resource.Builder builder, String method, Operation operation, Map<String, Schema> definitions) {
        LOGGER.debug("adding operation for `" + pathString + "` " + method);
        if (operation.getRequestBody() != null){
            RequestBody body = operation.getRequestBody();
            if(body.getContent() != null){
                Map<String, io.swagger.v3.oas.models.media.MediaType> content = body.getContent();
                for (String mediaType: content.keySet()){
                    if (content.get(mediaType) != null){
                        OpenAPIOperationController controller = createController(pathString,method,operation, mediaType,definitions);
                        addConsumesToResource(mediaType,builder,method,controller);
                    }
                }
            }
        }else {
            OpenAPIOperationController controller = createController(pathString,method,operation, "",definitions);
            addConsumesToResource("",builder,method,controller);

        }
    }

    private OpenAPIOperationController createController(String pathString, String method, Operation operation, String mediaType, Map<String, Schema> definitions) {
        OpenAPIOperationController controller = new OpenAPIOperationController(config, pathString, method, operation, mediaType, definitions);
        if (controller.getMethod() == null) {
            if (controller.getMethodName() != null) {
                List<String> missingMethods = missingOperations.get(controller.getControllerName());
                if (missingMethods == null) {
                    missingMethods = new ArrayList<>();
                    missingOperations.put(controller.getControllerName(), missingMethods);
                }
                missingMethods.add(controller.getOperationSignature());
            }
        }
        unimplementedMappedModels.addAll(controller.getUnimplementedMappedModels());
        return controller;
    }

    private void addConsumesToResource(String mediaType, Resource.Builder builder, String method, OpenAPIOperationController controller) {
        if (StringUtils.isNotBlank(mediaType)) {
            try {
                MediaType media = MediaType.valueOf(mediaType);
                if (media.getSubtype().equals("yaml")) {
                    builder.addMethod(method).handledBy(controller);
                } else {
                    builder.addMethod(method).handledBy(controller).consumes(media.toString());
                }
            } catch (Exception e) {
                LOGGER.error("unable to find a matching mediatype for " + mediaType + " in " + controller.getMethodName());
            }
        }else{
            builder.addMethod(method).handledBy(controller);
        }
    }

    private OpenAPI getExposedAPI(Configuration config) {
        ExposedSpecOptions exposedSpecOptions =  config.getExposedSpecOptions();
        boolean hideExtension = exposedSpecOptions.isHideInflectorExtensions();
        OpenAPI exposedAPI = null;
        if (exposedSpecOptions.isUseOriginalNotParsed()) {
            exposedAPI = deserializeSpec(config.getSwaggerUrl());
        }
        if (exposedAPI == null) {
            SwaggerParseResult exposedSwaggerParseResult = new OpenAPIV3Parser().readLocation(config.getSwaggerUrl(), null, exposedSpecOptions.getParseOptions());
            exposedAPI = exposedSwaggerParseResult.getOpenAPI();
        }
        if (hideExtension) {
            new ExtensionsUtil().removeExtensions(exposedAPI);
        } else {
            new ExtensionsUtil().addExtensions(exposedAPI);
        }
        return exposedAPI;
    }

    // deserialize with swagger-core
    private OpenAPI deserializeSpec(String swaggerUrl) {
        OpenAPI exposedAPI = null;
        String location = config.getSwaggerUrl().replaceAll("\\\\", "/");
        try {
            String data = null;
            if (location.toLowerCase().startsWith("http")) {
                data = RemoteUrl.urlToString(location, null);
            } else {
                String fileScheme = "file:";
                Path path;
                if (location.toLowerCase().startsWith("file:")) {
                    path = Paths.get(URI.create(location));
                } else {
                    path = Paths.get(location);
                }

                if (Files.exists(path, new LinkOption[0])) {
                    data = FileUtils.readFileToString(path.toFile(), "UTF-8");
                } else {
                    data = ClasspathHelper.loadFileFromClasspath(location);
                }
                exposedAPI = getRightMapper(data).readValue(data, OpenAPI.class);
            }
        } catch (SSLHandshakeException e) {
            LOGGER.error("unable to read location `" + location + "` due to a SSL configuration error.  It is possible that the server SSL certificate is invalid, self-signed, or has an untrusted Certificate Authority.", e);
        } catch (Exception e1) {
            LOGGER.error("unable to read location `" + location + "`", e1);
        }
        return exposedAPI;
    }

    private ObjectMapper getRightMapper(String data) {
        ObjectMapper mapper;
        if (data.trim().startsWith("{")) {
            mapper = Json.mapper();
        } else {
            mapper = Yaml.mapper();
        }

        return mapper;
    }

}
