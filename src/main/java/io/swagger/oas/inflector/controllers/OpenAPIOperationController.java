/*
 *  Copyright 2017 SmartBear Software
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

package io.swagger.oas.inflector.controllers;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.oas.inflector.config.Configuration;
import io.swagger.oas.inflector.config.ControllerFactory;
import io.swagger.oas.inflector.converters.ConversionException;
import io.swagger.oas.inflector.converters.InputConverter;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.models.ArrayExample;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.examples.models.ObjectExample;
import io.swagger.oas.inflector.models.ApiError;
import io.swagger.oas.inflector.models.RequestContext;
import io.swagger.oas.inflector.models.ResponseContext;
import io.swagger.oas.inflector.processors.EntityProcessor;
import io.swagger.oas.inflector.processors.EntityProcessorFactory;
import io.swagger.oas.inflector.schema.SchemaValidator;
import io.swagger.oas.inflector.utils.ApiErrorUtils;
import io.swagger.oas.inflector.utils.ApiException;
import io.swagger.oas.inflector.utils.ContentTypeSelector;
import io.swagger.oas.inflector.utils.ReflectionUtils;
import io.swagger.oas.inflector.validators.ValidationException;
import io.swagger.oas.inflector.validators.ValidationMessage;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.core.util.Json;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class OpenAPIOperationController extends ReflectionUtils implements Inflector<ContainerRequestContext, Response> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIOperationController.class);
    private static final String RANDOM_EXAMPLE =  "random";
    private static final String SEQUENCIAL_EXAMPLE =  "sequence";
    private int sequence = 0;

    private static Set<String> commonHeaders = new HashSet<String>();

    static {
        commonHeaders.add("Host");
        commonHeaders.add("User-Agent");
        commonHeaders.add("Accept");
        commonHeaders.add("Content-Type");
        commonHeaders.add("Content-Length");
    }

    private String path;
    private String httpMethod;
    private Operation operation;
    private Object controller = null;
    private Method method = null;
    private JavaType[] parameterClasses = null;
    private Map<String, Schema> definitions;
    private InputConverter validator;
    private String controllerName;
    private String methodName;
    private String operationSignature;

    @Inject
    private Provider<Providers> providersProvider;
    @Inject
    private Provider<HttpServletRequest> requestProvider;
    @Inject
    private Provider<HttpServletResponse> responseProvider;
    private ControllerFactory controllerFactoryCache = null;

    public OpenAPIOperationController(Configuration config, String path, String httpMethod, Operation operation, String mediaType, Map<String, Schema> definitions) {
        this.setConfiguration(config);
        this.path = path;
        this.httpMethod = httpMethod;
        this.operation = operation;
        this.definitions = definitions;
        this.validator = InputConverter.getInstance();
        this.method = detectMethod(operation, mediaType);
        if (method == null) {
            LOGGER.debug("no method `" + methodName + "` in `" + controllerName + "` to map to, using mock response");
        }
    }

    // Used for unit testing only
    OpenAPIOperationController(Configuration config, String path, String httpMethod, Operation operation,
                                             String mediaType, Map<String, Schema> definitions,
                                             Provider<HttpServletRequest> requestProvider,
                                             Provider<HttpServletResponse> responseProvider) {
        this(config, path, httpMethod, operation, mediaType, definitions);
        this.requestProvider = requestProvider;
        this.responseProvider = responseProvider;
    }

    public Operation getOperation() {
        return operation;
    }

    public Object getController() {
        return controller;
    }

    public JavaType[] getParameterClasses() {
        return parameterClasses;
    }

    public Map<String, Schema> getDefinitions() {
        return definitions;
    }

    public InputConverter getValidator() {
        return validator;
    }

    public Method detectMethod(Operation operation, String mediaType) {
        controllerName = getControllerName(operation);
        methodName = getMethodName(path, httpMethod, operation);
        JavaType[] args = getOperationParameterClasses(operation, mediaType, definitions);

        buildOperationSignature(args);

        LOGGER.info("looking for method: `" + operationSignature + "` in class `" + controllerName + "`");
        parameterClasses = args;

        return matchBuildMethodToImplemented(args);
    }

    private Method matchBuildMethodToImplemented(JavaType[] args) {

        if (controllerName != null && methodName != null) {
            try {
                Class<?> cls;
                try {
                    cls = Class.forName(controllerName);
                } catch (ClassNotFoundException e) {
                    controllerName = controllerName + "Controller";
                    cls = Class.forName(controllerName);
                }

                Method[] methods = cls.getMethods();
                for (Method method : methods) {
                    if (methodName.equals(method.getName())) {
                        Class<?>[] methodArgs = method.getParameterTypes();
                        if (methodArgs.length == args.length) {
                            boolean matched = true;
                            for (int i = 0; i < methodArgs.length; i++) {
                                if (!args[i].getRawClass().equals(methodArgs[i])) {
                                    //validate if its InputStream and change it to the implemented method in Controller
                                    if (args[i].getRawClass().equals(InputStream.class)){
                                        args[i] = updateArgumentClass(methodArgs[i]);
                                    }else {
                                        LOGGER.debug("failed to match " + args[i] + ", " + methodArgs[i]);
                                        matched = false;
                                    }
                                }
                            }
                            if (matched) {
                                parameterClasses = args;
                                controller = getControllerFactory().instantiateController(cls, operation);
                                LOGGER.debug("found class `" + controllerName + "`");
                                //update operationSignature
                                buildOperationSignature(args);
                                return method;
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                LOGGER.debug("didn't find class " + controller);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private void buildOperationSignature(JavaType[] args) {
        StringBuilder builder = new StringBuilder();

        builder.append(getMethodName(path, httpMethod, operation))
                .append("(");

        for (int i = 0; i < args.length; i++) {
            if (i == 0) {
                builder.append(RequestContext.class.getCanonicalName()).append(" request");
            } else {
                builder.append(", ");
                if (args[i] == null) {
                    LOGGER.error("didn't expect a null class for the argument in operation " + operation.getOperationId());
                } else if (args[i].getRawClass() != null) {
                    String className = args[i].getRawClass().getName();
                    if (className.startsWith("java.lang.")) {
                        className = className.substring("java.lang.".length());
                    }
                    builder.append(className);

                    // TODO: we should really show the actual parameter name here
                    builder.append(" ").append("p" + i);
                }
            }
        }
        builder.append(")");

        operationSignature = "public ResponseContext " + builder.toString();
    }

    @Override
    public Response apply(ContainerRequestContext ctx) {
        List<Parameter> parameters = operation.getParameters();

        final RequestContext requestContext = createContext(ctx);
        Map<String, File> inputStreams = new HashMap<>();

        Object[] args = new Object[parameterClasses.length];

        int i = 0;

        args[i] = requestContext;

        i += 1;
        List<ValidationMessage> missingParams = new ArrayList<>();
        UriInfo uri = ctx.getUriInfo();
        Set<String> existingKeys = new HashSet<>();

        for (Iterator<String> x = uri.getQueryParameters().keySet().iterator(); x.hasNext(); ) {
            existingKeys.add(x.next() + ": qp");
        }
        for (Iterator<String> x = uri.getPathParameters().keySet().iterator(); x.hasNext(); ) {
            existingKeys.add(x.next() + ": pp");
        }


        MediaType mediaType = requestContext.getMediaType();
        Object argument = null;

        if (parameters != null && parameters.size() > 0) {
            for (Parameter parameter : parameters) {
                String in = parameter.getIn();
                try {
                    try {
                        String paramName = parameter.getName();
                        if ("query".equals(in)) {
                            existingKeys.remove(paramName + ": qp");
                        }
                        if ("path".equals(in)) {
                            existingKeys.remove(paramName + ": pp");
                        }
                        JavaType jt = parameterClasses[i];
                        Class<?> cls = jt.getRawClass();
                        if ("query".equals(in)) {
                            argument = validator.convertAndValidate(uri.getQueryParameters().get(parameter.getName()), parameter, cls, definitions);
                        } else if ("path".equals(in)) {
                            argument = validator.convertAndValidate(uri.getPathParameters().get(parameter.getName()), parameter, cls, definitions);
                        } else if ("header".equals(in)) {
                            argument = validator.convertAndValidate(ctx.getHeaders().get(parameter.getName()), parameter, cls, definitions);
                        }
                    } catch (ConversionException e) {
                        missingParams.add(e.getError());
                    } catch (ValidationException e) {
                        missingParams.add(e.getValidationMessage());
                    }

                } catch (NumberFormatException e) {
                    LOGGER.error("Couldn't find " + parameter.getName() + " (" + in + ") to " + parameterClasses[i], e);
                }

                args[i] = argument;
                i += 1;
            }
        }

        if (operation.getRequestBody() != null) {
            RequestBody body = operation.getRequestBody();

            if (ctx.hasEntity()) {
                JavaType jt = parameterClasses[i];
                Class<?> cls = null;
                if(jt != null) {
                    cls  = jt.getRawClass();
                }
                try {
                    argument = EntityProcessorFactory.readValue(mediaType, ctx.getEntityStream(), cls,this);

                    if (argument != null) {
                        if (mediaType.isCompatible(MediaType.APPLICATION_FORM_URLENCODED_TYPE) ||
                                mediaType.isCompatible(MediaType.MULTIPART_FORM_DATA_TYPE) ||
                                mediaType.isCompatible(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {

                            if (argument instanceof Object[]) {
                                Object[] args2 = (Object[]) argument;
                                // populate with request context and any other parameters
                                for (int ii = 0; ii < args.length; ii++) {
                                    if (args[ii] != null) {
                                        args2[ii] = args[ii];
                                    }
                                }
                                args = args2;
                            } else {
                                args[i] = argument;
                            }
                        } else {
                            if (body.getContent() != null) {
                                Content content = body.getContent();
                                io.swagger.v3.oas.models.media.MediaType media = content.get(mediaType.toString());

                                if (media == null) {
                                    media = content.get(MediaType.WILDCARD);
                                }

                                if (media != null) {
                                    if (media.getSchema() != null) {
                                        validate(argument, media.getSchema(), SchemaValidator.Direction.INPUT);
                                    }
                                }

                                args[i] = argument;
                                i += 1;

                            }
                        }
                    } else if (operation.getRequestBody().getRequired()) {
                        ValidationException e = new ValidationException();
                        e.message(new ValidationMessage()
                                .message("The input body `" + operation.getRequestBody() + "` is required but was provided with an unsupported media type `" + 
                                            mediaType + "`"));
                        try {
                            throw e;
                        } catch (ValidationException e1) {
                            missingParams.add(e.getValidationMessage());
                        }
                    }
                } catch (ConversionException e) {
                    missingParams.add(e.getError());
                }

            } else if (operation.getRequestBody().getRequired()) {
                ValidationException e = new ValidationException();
                e.message(new ValidationMessage()
                        .message("The input body `" + operation.getRequestBody() + "` is required"));
                try {
                    throw e;
                } catch (ValidationException e1) {
                    missingParams.add(e.getValidationMessage());
                }
            }
        }

        if (existingKeys.size() > 0) {
            LOGGER.debug("unexpected keys: " + existingKeys);
        }
        if (missingParams.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("Input error");
            if (missingParams.size() > 1) {
                builder.append("s");
            }
            builder.append(": ");
            int count = 0;
            for (ValidationMessage message : missingParams) {
                if (count > 0) {
                    builder.append(", ");
                }
                if (message != null && message.getMessage() != null) {
                    builder.append(message.getMessage());
                } else {
                    builder.append("no additional input");
                }
                count += 1;
            }
            int statusCode = config.getInvalidRequestStatusCode();
            ApiError error = new ApiError()
                    .code(statusCode)
                    .message(builder.toString());
            throw new ApiException(error);
        }
        
        try {
            if (method != null) {
                LOGGER.info("calling method " + method + " on controller " + this.controller + " with args " + Arrays.toString(args));
                try {
                    Object response = method.invoke(controller, args);
                    if (response instanceof ResponseContext) {
                        ResponseContext wrapper = (ResponseContext) response;
                        setResponseContentType(requestContext, wrapper, operation);
                        ResponseBuilder builder = Response.status(wrapper.getStatus());

                        // response headers
                        for (String key : wrapper.getHeaders().keySet()) {
                            List<String> v = wrapper.getHeaders().get(key);
                            if (v.size() == 1) {
                                builder.header(key, v.get(0));
                            } else {
                                builder.header(key, v);
                            }
                        }

                        // entity
                        if (wrapper.getEntity() != null) {
                            builder.entity(wrapper.getEntity());
                            // content type
                            if (wrapper.getContentType() != null) {
                                builder.type(wrapper.getContentType());
                            } else {
                                final ContextResolver<ContentTypeSelector> selector = providersProvider
                                        .get().getContextResolver(ContentTypeSelector.class,
                                                MediaType.WILDCARD_TYPE);
                                if (selector != null) {
                                    selector.getContext(getClass()).apply(ctx.getAcceptableMediaTypes(),
                                            builder);
                                }
                            }

                            if (operation.getResponses() != null) {
                                String responseCode = String.valueOf(wrapper.getStatus());
                                ApiResponse responseSchema = operation.getResponses().get(responseCode);
                                if (responseSchema == null) {
                                    // try default response schema
                                    responseSchema = operation.getResponses().get("default");
                                }
                                if (responseSchema != null ) {
                                    if(responseSchema.getContent() != null) {
                                        for(String name: responseSchema.getContent().keySet()) {
                                            if(responseSchema.getContent().get(name).getSchema() != null) {
                                                validate(wrapper.getEntity(), responseSchema.getContent().get(name).getSchema(), SchemaValidator.Direction.OUTPUT);
                                            }
                                        }
                                    }
                                } else {
                                    LOGGER.debug("no response schema for code " + responseCode + " to validate against");
                                }
                            }
                        }

                        return builder.build();
                    } else {
                        MediaType type = identifyResponseContentType(requestContext, operation);
                        if (type != null) {
                            return Response.ok(response, type).entity(response).build();
                        } else {
                            return Response.ok(response).entity(response).build();
                        }
                    }

                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    for (Throwable cause = e.getCause(); cause != null; ) {
                        if (cause instanceof ApiException) {
                            throw (ApiException) cause;
                        }
                        final Throwable next = cause.getCause();
                        cause = next == cause || next == null ? null : next;
                    }
                    throw new ApiException(ApiErrorUtils.createInternalError(), e);
                }
            }
            Map<String, ApiResponse> responses = operation.getResponses();
            if (responses != null) {
                String[] keys = new String[responses.keySet().size()];
                Arrays.sort(responses.keySet().toArray(keys));
                int code = 0;
                String defaultKey = null;
                for (String key : keys) {
                    if (key.startsWith("2")) {
                        defaultKey = key;
                        code = Integer.parseInt(key);
                        break;
                    }
                    if ("default".equals(key)) {
                        defaultKey = key;
                        code = 200;
                        break;
                    }
                    if (key.startsWith("3")) {
                        // we use the 3xx responses as defaults
                        defaultKey = key;
                        code = Integer.parseInt(key);
                    }
                }

                if(defaultKey != null) {
                    ResponseBuilder builder = Response.status(code);
                    ApiResponse response = responses.get(defaultKey);

                    if(response.getHeaders() != null && response.getHeaders().size() > 0) {
                        Object output = null;
                        for(String key: response.getHeaders().keySet()) {
                            Header headerProperty = response.getHeaders().get(key);
                            if(headerProperty.getSchema()!= null){
                                output = ExampleBuilder.fromSchema(headerProperty.getSchema(), definitions, config.isAllowNullExamples());

                                if(output instanceof ArrayExample) {
                                    output = ((ArrayExample)output).asString();
                                }
                                else if(output instanceof ObjectExample) {
                                    LOGGER.debug("not serializing output example, only primitives or arrays of primitives are supported");
                                }
                                else {
                                    output = ((Example)output).asString();
                                }
                                builder.header(key, output);

                            }
                        }
                    }

                    Map<String, io.swagger.v3.oas.models.examples.Example> examples = new HashMap<>();
                    Object output = null;
                    List<String> exampleProcessorList = config.getExampleProcessors();
                    io.swagger.v3.oas.models.examples.Example outputExample = null;

                    if (response.getContent() != null) {
                        if (requestContext.getHeaders().get("Accept") != null) {
                            for (String acceptable : requestContext.getHeaders().get("Accept")) {
                                if (response.getContent().get(acceptable) != null) {
                                    if (response.getContent().get(acceptable).getExamples() != null) {
                                        examples = response.getContent().get(acceptable).getExamples();
                                    }
                                    if (examples != null && examples.size() > 0) {
                                        for (MediaType key : requestContext.getAcceptableMediaTypes()) {
                                            MediaType media = MediaType.valueOf(acceptable);
                                            if (media.isCompatible(key)) {
                                                if (exampleProcessorList != null && exampleProcessorList.size() > 0) {
                                                    for (String mode : exampleProcessorList) {
                                                        if (mode.equals(RANDOM_EXAMPLE)) {
                                                            Random generator = new Random();
                                                            Object[] values = examples.values().toArray();
                                                            outputExample = (io.swagger.v3.oas.models.examples.Example) values[generator.nextInt(values.length)];

                                                        } else if (mode.equals(SEQUENCIAL_EXAMPLE)) {
                                                            if (sequence >= examples.size()) {
                                                                sequence = 0;
                                                            }
                                                            Object[] values = examples.values().toArray();
                                                            outputExample = (io.swagger.v3.oas.models.examples.Example) values[sequence];
                                                            sequence++;
                                                        }
                                                        builder.entity(outputExample)
                                                                .type(acceptable);
                                                        return builder.build();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    output = ExampleBuilder.fromSchema(response.getContent().get(acceptable).getSchema(), definitions, config.isAllowNullExamples());
                                }else{
                                    for (String media: response.getContent().keySet()) {
                                        output = ExampleBuilder.fromSchema(response.getContent().get(media).getSchema(), definitions, config.isAllowNullExamples());
                                        break;
                                    }
                                }
                            }

                        }else{
                            for (String key: response.getContent().keySet()) {
                                if (response.getContent().get(key).getExamples() != null) {
                                    examples = response.getContent().get(key).getExamples();
                                }
                                if (examples != null && examples.size() > 0) {
                                    for (MediaType media : requestContext.getAcceptableMediaTypes()) {
                                        if (MediaType.valueOf(key).isCompatible(media)) {
                                            if (exampleProcessorList != null && exampleProcessorList.size() > 0) {
                                                for (String mode : exampleProcessorList) {
                                                    if (mode.equals(RANDOM_EXAMPLE)) {
                                                        Random generator = new Random();
                                                        Object[] values = examples.values().toArray();
                                                        outputExample = (io.swagger.v3.oas.models.examples.Example) values[generator.nextInt(values.length)];

                                                    } else if (mode.equals(SEQUENCIAL_EXAMPLE)) {
                                                        if (sequence >= examples.size()) {
                                                            sequence = 0;
                                                        }
                                                        Object[] values = examples.values().toArray();
                                                        outputExample = (io.swagger.v3.oas.models.examples.Example) values[sequence];
                                                        sequence++;
                                                    }
                                                    builder.entity(outputExample)
                                                            .type(MediaType.valueOf(key));
                                                    return builder.build();
                                                }
                                            }
                                        }
                                    }
                                }
                                output = ExampleBuilder.fromSchema(response.getContent().get(key).getSchema(), definitions, config.isAllowNullExamples());
                            }
                        }
                    }

                    if (output != null) {
                        ResponseContext resp = new ResponseContext().entity(output);
                        setResponseContentType(requestContext, resp, operation);
                        builder.entity(output);
                        if (resp.getContentType() != null) {
                            // this comes from the operation itself
                            builder.type(resp.getContentType());
                        }
                        else {
                            // get acceptable content types
                            List<EntityProcessor> processors = EntityProcessorFactory.getProcessors();

                            MediaType responseMediaType = null;

                            // take first compatible one
                            for (EntityProcessor processor : processors) {
                                if(responseMediaType != null) {
                                    break;
                                }
                                for (MediaType mediaTypet : requestContext.getAcceptableMediaTypes()) {
                                    LOGGER.debug("checking type " + mediaType.toString() + " against " + processor.getClass().getName());
                                    if (processor.supports(mediaType)) {
                                        builder.type(mediaTypet);
                                        responseMediaType = mediaTypet;
                                        break;
                                    }
                                }
                            }

                            if(responseMediaType == null) {
                                // no match based on Accept header, use first processor in list
                                for (EntityProcessor processor : processors) {
                                    List<MediaType> supportedTypes = processor.getSupportedMediaTypes();
                                    if (supportedTypes.size() > 0) {
                                        builder.type(supportedTypes.get(0));
                                        break;
                                    }
                                }
                            }
                        }

                        builder.entity(output);
                    }
                    return builder.build();
                }
                else {
                    LOGGER.debug("no response type to map to, assume 200");
                    code = 200;
                }
                return Response.status(code).build();
            }
            return Response.ok().build();
        } finally {
            for (String key : inputStreams.keySet()) {
                File file = inputStreams.get(key);
                if (file != null) {
                    LOGGER.debug("deleting file " + file.getPath());
                    file.delete();
                }
            }
        }
    }



    public void validate(Object o, Schema property, SchemaValidator.Direction direction) throws ApiException {
        doValidation(o, property, direction);
    }

    public void setResponseContentType(RequestContext res, ResponseContext resp, Operation operation) {
        // honor what has been set, it may be determined by business logic in the controller
        if (resp.getContentType() != null) {
            return;
        }
        MediaType type = identifyResponseContentType(res, operation);
        if (type != null) {
            resp.setContentType(type);
        }
    }

    public MediaType identifyResponseContentType(RequestContext res, Operation operation) {
        MediaType type = null;
        ApiResponses responses = operation.getResponses();
        if (responses != null) {
            for (String responseCode : responses.keySet()) {
                final ApiResponse response = responses.get(responseCode);
                Content content = response.getContent();
                if(content == null) {
                    continue;
                }

                for(String key : content.keySet()) {
                    MediaType mediaType = MediaType.valueOf(key);
                    if (res.getHeaders().get("Accept")!= null) {
                        for (String acceptable : res.getHeaders().get("Accept")) {
                            String subtype = acceptable.substring(acceptable.lastIndexOf("/") + 1);
                            if (!MediaType.WILDCARD_TYPE.equals(mediaType)) {
                                type = mediaType;
                                if (subtype.equals(mediaType.getSubtype())) {
                                    return type;
                                }
                            }

                        }
                    }else {
                        for (MediaType acceptable : res.getAcceptableMediaTypes()){
                            if (!MediaType.WILDCARD_TYPE.equals(mediaType)) {
                                type = mediaType;
                                if (mediaType.isCompatible(acceptable)) {
                                    return type;
                                }

                            }
                        }
                    }

                }

            }

        }
        return type;
    }

    public String getOperationSignature() {
        return operationSignature;
    }

    public void setOperationSignature(String operationSignature) {
        this.operationSignature = operationSignature;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    // package protected to facilitate unit testing
    RequestContext createContext(ContainerRequestContext from) {
        HttpServletRequest request = getHttpServletRequest();
        HttpServletResponse response = getHttpServletResponse();
        return new RequestContext(from, request, response);
    }

    private HttpServletRequest getHttpServletRequest() {
        if (requestProvider != null) {
            return requestProvider.get();
        } else {
            LOGGER.warn("HttpServletRequest provider was null - returning null request!");
            return null;
        }
    }

    private HttpServletResponse getHttpServletResponse() {
        if (responseProvider != null) {
            return responseProvider.get();
        } else {
            LOGGER.warn("HttpServletResponse provider was null - returning null response!");
            return null;
        }
    }

    private void doValidation(Object value, Object schema, SchemaValidator.Direction direction) throws ApiException {
        if (config.getValidatePayloads().isEmpty()) {
            return;
        }
        switch (direction) {
            case INPUT:
                if (config.getValidatePayloads().contains(Configuration.Direction.IN)
                        && !SchemaValidator.validate(value, Json.pretty(schema), direction)) {
                    throw new ApiException(new ApiError()
                            .code(config.getInvalidRequestStatusCode())
                            .message("Input does not match the expected structure"));
                }
                break;
            case OUTPUT:
                if (config.getValidatePayloads().contains(Configuration.Direction.OUT)
                        && !SchemaValidator.validate(value, Json.pretty(schema), direction)) {
                    throw new ApiException(new ApiError()
                            .code(config.getInvalidRequestStatusCode())
                            .message("The server generated an invalid response"));
                }
                break;
        }
    }
    
    private ControllerFactory getControllerFactory() {
    	if (controllerFactoryCache == null){
    		controllerFactoryCache = config.getControllerFactory();
    	}
    	return controllerFactoryCache;
    }




}
