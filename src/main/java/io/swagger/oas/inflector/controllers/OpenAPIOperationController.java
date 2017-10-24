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
import com.google.common.io.Files;
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
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.headers.Header;
import io.swagger.oas.models.media.Content;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;
import io.swagger.oas.models.responses.ApiResponse;
import io.swagger.oas.models.responses.ApiResponses;
import io.swagger.util.Json;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    private JavaType[] requestBodyClass = null;
    private Map<String, Schema> definitions;
    private InputConverter validator;
    private String controllerName;
    private String methodName;
    private String operationSignature;
    @Inject
    private Provider<Providers> providersProvider;
    @Inject
    private Provider<HttpServletRequest> requestProvider;
    private ControllerFactory controllerFactoryCache = null;

    public OpenAPIOperationController(Configuration config, String path, String httpMethod, Operation operation, Map<String, Schema> definitions) {
        this.setConfiguration(config);
        this.path = path;
        this.httpMethod = httpMethod;
        this.operation = operation;
        this.definitions = definitions;
        this.validator = InputConverter.getInstance();
        this.method = detectMethod(operation);
        if (method == null) {
            LOGGER.debug("no method `" + methodName + "` in `" + controllerName + "` to map to, using mock response");
        }
    }

    public Method detectMethod(Operation operation) {
        controllerName = getControllerName(operation);
        methodName = getMethodName(path, httpMethod, operation);
        JavaType[] args = getOperationParameterClasses(operation, this.definitions);
        JavaType[] args2 = getOperationRequestBodyClasses(operation, this.definitions);

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
                    builder.append(" ").append(args[1]);

                }
            }
        }
        builder.append(")");

        operationSignature = "public ResponseContext " + builder.toString();

        LOGGER.info("looking for method: `" + operationSignature + "` in class `" + controllerName + "`");
        this.parameterClasses = args;
        this.requestBodyClass = args2;

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
                            int i = 0;
                            boolean matched = true;
                            if (!args[i].getRawClass().equals(methodArgs[i])) {
                                LOGGER.debug("failed to match " + args[i] + ", " + methodArgs[i]);
                                matched = false;
                            }
                            if (matched) {
                                this.parameterClasses = args;
                                this.controller = getControllerFactory().instantiateController(cls, operation);
                                LOGGER.debug("found class `" + controllerName + "`");
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

    @Override
    public Response apply(ContainerRequestContext ctx) {
        List<Parameter> parameters = operation.getParameters();
        int requestBody = 0;
        if(operation.getRequestBody() != null){
            requestBody = 1;
        }
        int arguments = parameters.size() + requestBody;
        final RequestContext requestContext = createContext(ctx);

        Map<String, File> inputStreams = new HashMap<>();

        Object[] args = new Object[arguments + 1];

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

        for (Iterator<String> x = ctx.getHeaders().keySet().iterator(); x.hasNext(); ) {
            String key = x.next();
//              if(!commonHeaders.contains(key))
//                existingKeys.add(key);
        }
        //MediaType mt = requestContext.getMediaType();



        if (operation.getRequestBody() != null) {
            Object o = null;
            JavaType jt = requestBodyClass[i];
            Class<?> cls = jt.getRawClass();
            if (ctx.hasEntity()) {
                RequestBody body = operation.getRequestBody();
                try {
                    o = EntityProcessorFactory.readValue(ctx.getMediaType(), ctx.getEntityStream(), cls);
                    if (o != null) {
                        if (body.getContent() != null) {
                            Content content = body.getContent();
                            for (String key : content.keySet()) {
                                io.swagger.oas.models.media.MediaType mediaType = content.get(key);
                                if (mediaType.getSchema() != null) {
                                    validate(o, mediaType.getSchema(), SchemaValidator.Direction.INPUT);
                                }
                            }

                        }

                        if (parameters == null || parameters.size() == 0) {
                            args[i] = o;
                            i += 1;
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




        if (parameters != null) {
            for (Parameter parameter : parameters) {
                String in = parameter.getIn();
                Object o = null;

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
                            o = validator.convertAndValidate(uri.getQueryParameters().get(parameter.getName()), parameter, cls, definitions);
                        } else if ("path".equals(in)) {
                            o = validator.convertAndValidate(uri.getPathParameters().get(parameter.getName()), parameter, cls, definitions);
                        } else if ("header".equals(in)) {
                            o = validator.convertAndValidate(ctx.getHeaders().get(parameter.getName()), parameter, cls, definitions);
                        }
                    } catch (ConversionException e) {
                        missingParams.add(e.getError());
                    } catch (ValidationException e) {
                        missingParams.add(e.getValidationMessage());
                    }

                } catch (NumberFormatException e) {
                    LOGGER.error("Couldn't find " + parameter.getName() + " (" + in + ") to " + parameterClasses[i], e);
                }

                args[i] = o;
                i += 1;
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
        }
        try {
            if (method != null) {
                LOGGER.info("calling method " + method + " on controller " + this.controller + " with args " + Arrays.toString(args));
                try {
                    Object response = method.invoke(controller, args);
                    if (response instanceof ResponseContext) {
                        ResponseContext wrapper = (ResponseContext) response;
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
                    }
                    return Response.ok().entity(response).build();
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
                        Schema property = null;
                        Object output = null;
                        for(String key: response.getHeaders().keySet()) {
                            Header headerProperty = response.getHeaders().get(key);
                            if(headerProperty.getSchema()!= null){
                                output = ExampleBuilder.fromSchema(headerProperty.getSchema(), definitions);

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

                    Map<String, io.swagger.oas.models.examples.Example> examples = new HashMap<>();
                    Object output = null;
                    List<String> exampleProcessorList = config.getExampleProcessors();
                    io.swagger.oas.models.examples.Example outputExample = null;

                    if (response.getContent() != null) {
                        if (requestContext.getHeaders().get("Content-Type") != null) {
                            for (String acceptable : requestContext.getHeaders().get("Content-Type")) {
                                if (response.getContent().get(acceptable) != null) {
                                    if (response.getContent().get(acceptable).getExamples() != null) {
                                        examples = response.getContent().get(acceptable).getExamples();
                                    }
                                    if (examples != null && examples.size() > 0) {
                                        for (MediaType mediaType : requestContext.getAcceptableMediaTypes()) {
                                            MediaType media = MediaType.valueOf(acceptable);
                                            if (media.isCompatible(mediaType)) {
                                                if (exampleProcessorList != null && exampleProcessorList.size() > 0) {
                                                    for (String mode : exampleProcessorList) {
                                                        if (mode.equals(RANDOM_EXAMPLE)) {
                                                            Random generator = new Random();
                                                            Object[] values = examples.values().toArray();
                                                            outputExample = (io.swagger.oas.models.examples.Example) values[generator.nextInt(values.length)];

                                                        } else if (mode.equals(SEQUENCIAL_EXAMPLE)) {
                                                            if (sequence >= examples.size()) {
                                                                sequence = 0;
                                                            }
                                                            Object[] values = examples.values().toArray();
                                                            outputExample = (io.swagger.oas.models.examples.Example) values[sequence];
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
                                    output = ExampleBuilder.fromSchema(response.getContent().get(acceptable).getSchema(), definitions);
                                }else{
                                    throw new ApiException(ApiErrorUtils.createInternalError());
                                }
                            }
                        }else{
                            for (String key: response.getContent().keySet()) {
                                if (response.getContent().get(key).getExamples() != null) {
                                    examples = response.getContent().get(key).getExamples();
                                }
                                if (examples != null && examples.size() > 0) {
                                    for (MediaType mediaType : requestContext.getAcceptableMediaTypes()) {
                                        if (MediaType.valueOf(key).isCompatible(mediaType)) {
                                            if (exampleProcessorList != null && exampleProcessorList.size() > 0) {
                                                for (String mode : exampleProcessorList) {
                                                    if (mode.equals(RANDOM_EXAMPLE)) {
                                                        Random generator = new Random();
                                                        Object[] values = examples.values().toArray();
                                                        outputExample = (io.swagger.oas.models.examples.Example) values[generator.nextInt(values.length)];

                                                    } else if (mode.equals(SEQUENCIAL_EXAMPLE)) {
                                                        if (sequence >= examples.size()) {
                                                            sequence = 0;
                                                        }
                                                        Object[] values = examples.values().toArray();
                                                        outputExample = (io.swagger.oas.models.examples.Example) values[sequence];
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
                                output = ExampleBuilder.fromSchema(response.getContent().get(key).getSchema(), definitions);
                            }
                        }
                    }
                    if (output != null) {
                        ResponseContext resp = new ResponseContext().entity(output);
                        setContentType(requestContext, resp, operation);
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
                                for (MediaType mt : requestContext.getAcceptableMediaTypes()) {
                                    LOGGER.debug("checking type " + mt.toString() + " against " + processor.getClass().getName());
                                    if (processor.supports(mt)) {
                                        builder.type(mt);
                                        responseMediaType = mt;
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

    static String extractFilenameFromHeaders(Map<String, String> headers) {
        String filename = headers.get("filename");
        if( StringUtils.isBlank( filename )){
            return null;
        }

        filename = filename.trim();

        int ix = filename.lastIndexOf(File.separatorChar);
        if (ix != -1 ) {
            filename = filename.substring(ix + 1).trim();
            if( StringUtils.isBlank(filename)){
                return null;
            }
        }

        return filename;
    }

    public void validate(Object o, Schema property, SchemaValidator.Direction direction) throws ApiException {
        doValidation(o, property, direction);
    }

    public void setContentType(RequestContext res, ResponseContext resp, Operation operation) {
        // honor what has been set, it may be determined by business logic in the controller
       if (resp.getContentType() != null) {
            return;
        }
        ApiResponses responses = operation.getResponses();
        if (responses != null) {
            for (String responseCode : responses.keySet()) {
                final ApiResponse response = responses.get(responseCode);
                Content content = response.getContent();
                if(content == null) {
                    return;
                }

                for(String key : content.keySet()) {
                    MediaType mediaType = MediaType.valueOf(key);
                    if (res.getHeaders().get("Content-Type")!= null) {
                        for (String acceptable : res.getHeaders().get("Content-Type")) {
                            String subtype = acceptable.substring(acceptable.lastIndexOf("/") + 1);

                            if (subtype.equals(mediaType.getSubtype())) {
                                resp.setContentType(mediaType);
                                return;
                            }else {
                                resp.setContentType(mediaType);
                            }
                        }
                    }else {
                        for (MediaType acceptable : res.getAcceptableMediaTypes()){

                            if (mediaType.isCompatible(acceptable)) {
                                resp.setContentType(mediaType);
                                return;
                            }else {
                                resp.setContentType(mediaType);
                            }
                        }

                    }

                }

            }

        }
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

    private RequestContext createContext(ContainerRequestContext from) {
        final RequestContext result = new RequestContext(from);
        if (requestProvider != null) {
            final HttpServletRequest request = requestProvider.get();
            if (request != null) {
                result.setRemoteAddr(request.getRemoteAddr());
            }
        }
        return result;
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
