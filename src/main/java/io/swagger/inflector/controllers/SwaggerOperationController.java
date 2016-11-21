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

package io.swagger.inflector.controllers;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.io.Files;
import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.config.ControllerFactory;
import io.swagger.inflector.converters.ConversionException;
import io.swagger.inflector.converters.InputConverter;
import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.models.ArrayExample;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.examples.models.ObjectExample;
import io.swagger.inflector.models.ApiError;
import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import io.swagger.inflector.processors.EntityProcessor;
import io.swagger.inflector.processors.EntityProcessorFactory;
import io.swagger.inflector.schema.SchemaValidator;
import io.swagger.inflector.utils.ApiErrorUtils;
import io.swagger.inflector.utils.ApiException;
import io.swagger.inflector.utils.ContentTypeSelector;
import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.inflector.validators.ValidationException;
import io.swagger.inflector.validators.ValidationMessage;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.Property;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SwaggerOperationController extends ReflectionUtils implements Inflector<ContainerRequestContext, Response> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerOperationController.class);

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
    private Map<String, Model> definitions;
    private InputConverter validator;
    private String controllerName;
    private String methodName;
    private String operationSignature;
    @Inject
    private Provider<Providers> providersProvider;
    @Inject
    private Provider<HttpServletRequest> requestProvider;
    private ControllerFactory controllerFactoryCache = null;

    public SwaggerOperationController(Configuration config, String path, String httpMethod, Operation operation, Map<String, Model> definitions) {
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

        StringBuilder builder = new StringBuilder();

        builder.append(getMethodName(path, httpMethod, operation))
                .append("(");

        for (int i = 0; i < args.length; i++) {
            if (i == 0) {
                builder.append(RequestContext.class.getCanonicalName()).append(" request");
            } else {
                builder.append(", ");
                if (args[i] == null) {
                    LOGGER.error("didn't expect a null class for " + operation.getParameters().get(i - 1).getName());
                } else if (args[i].getRawClass() != null) {
                    String className = args[i].getRawClass().getName();
                    if (className.startsWith("java.lang.")) {
                        className = className.substring("java.lang.".length());
                    }
                    builder.append(className);
                    builder.append(" ").append(operation.getParameters().get(i - 1).getName());
                }
            }
        }
        builder.append(")");

        operationSignature = "public io.swagger.inflector.models.ResponseContext " + builder.toString();

        LOGGER.info("looking for method: `" + operationSignature + "` in class `" + controllerName + "`");
        this.parameterClasses = args;

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
        final RequestContext requestContext = createContext(ctx);

        String path = ctx.getUriInfo().getPath();
        Map<String, Map<String, String>> formMap = new HashMap<String, Map<String, String>>();
        Map<String, File> inputStreams = new HashMap<String, File>();

        Object[] args = new Object[parameters.size() + 1];
        if (parameters != null) {
            int i = 0;

            args[i] = requestContext;
            i += 1;
            List<ValidationMessage> missingParams = new ArrayList<ValidationMessage>();
            UriInfo uri = ctx.getUriInfo();
            String formDataString = null;
            String[] parts = null;
            Set<String> existingKeys = new HashSet<String>();

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
            MediaType mt = requestContext.getMediaType();

            for (Parameter p : parameters) {
                Map<String, String> headers = new HashMap<String, String>();
                String name = null;

                if (p instanceof FormParameter) {
                    if (formDataString == null) {
                        // can only read stream once
                        if (mt.isCompatible(MediaType.MULTIPART_FORM_DATA_TYPE)) {
                            // get the boundary
                            String boundary = mt.getParameters().get("boundary");

                            if (boundary != null) {
                                try {
                                    InputStream output = ctx.getEntityStream();

                                    MultipartStream multipartStream = new MultipartStream(output, boundary.getBytes());
                                    boolean nextPart = multipartStream.skipPreamble();
                                    while (nextPart) {
                                        String header = multipartStream.readHeaders();
                                        // process headers
                                        if (header != null) {
                                            CSVFormat format = CSVFormat.DEFAULT
                                                    .withDelimiter(';')
                                                    .withRecordSeparator("=");

                                            Iterable<CSVRecord> records = format.parse(new StringReader(header));
                                            for (CSVRecord r : records) {
                                                for (int j = 0; j < r.size(); j++) {
                                                    String string = r.get(j);

                                                    Iterable<CSVRecord> outerString = CSVFormat.DEFAULT
                                                            .withDelimiter('=')
                                                            .parse(new StringReader(string));
                                                    for (CSVRecord outerKvPair : outerString) {
                                                        if (outerKvPair.size() == 2) {
                                                            String key = outerKvPair.get(0).trim();
                                                            String value = outerKvPair.get(1).trim();
                                                            if ("name".equals(key)) {
                                                                name = value;
                                                            }
                                                            headers.put(key, value);
                                                        } else {
                                                            Iterable<CSVRecord> innerString = CSVFormat.DEFAULT
                                                                    .withDelimiter(':')
                                                                    .parse(new StringReader(string));
                                                            for (CSVRecord innerKVPair : innerString) {
                                                                if (innerKVPair.size() == 2) {
                                                                    String key = innerKVPair.get(0).trim();
                                                                    String value = innerKVPair.get(1).trim();
                                                                    if ("name".equals(key)) {
                                                                        name = value;
                                                                    }
                                                                    headers.put(key, value);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (name != null) {
                                                        formMap.put(name, headers);
                                                    }
                                                }
                                            }
                                        }
                                        String filename = extractFilenameFromHeaders( headers ) ;
                                        if (filename != null) {
                                            try {
                                                File file = new File(Files.createTempDir(), filename);
                                                file.deleteOnExit();
                                                file.getParentFile().deleteOnExit();
                                                FileOutputStream fo = new FileOutputStream(file);
                                                multipartStream.readBodyData(fo);
                                                inputStreams.put(name, file);
                                            }
                                            catch( Exception e){
                                                LOGGER.error("Failed to extract uploaded file", e );
                                            }
                                        } else {
                                            ByteArrayOutputStream bo = new ByteArrayOutputStream();
                                            multipartStream.readBodyData(bo);
                                            String value = bo.toString();
                                            headers.put(name, value);
                                        }
                                        if(name != null) {
                                            formMap.put(name, headers);
                                        }
                                        headers = new HashMap<>();
                                        name = null;
                                        nextPart = multipartStream.readBoundary();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            try {
                                formDataString = IOUtils.toString(ctx.getEntityStream(), "UTF-8");
                                parts = formDataString.split("&");

                                for (String part : parts) {
                                    String[] kv = part.split("=");
                                    existingKeys.add(kv[0] + ": fp");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            for (Parameter parameter : parameters) {
                String in = parameter.getIn();
                Object o = null;

                try {
                    if ("formData".equals(in)) {
                        SerializableParameter sp = (SerializableParameter) parameter;
                        String name = parameter.getName();
                        if (mt.isCompatible(MediaType.MULTIPART_FORM_DATA_TYPE)) {
                            // look in the form map
                            Map<String, String> headers = formMap.get(name);
                            if (headers != null && headers.size() > 0) {
                                if ("file".equals(sp.getType())) {
                                    o = inputStreams.get(name);
                                } else {
                                    Object obj = headers.get(parameter.getName());
                                    if (obj != null) {
                                        JavaType jt = parameterClasses[i];
                                        Class<?> cls = jt.getRawClass();

                                        List<String> os = Arrays.asList(obj.toString());
                                        try {
                                            o = validator.convertAndValidate(os, parameter, cls, definitions);
                                        } catch (ConversionException e) {
                                            missingParams.add(e.getError());
                                        } catch (ValidationException e) {
                                            missingParams.add(e.getValidationMessage());
                                        }
                                    }
                                }
                            }
                        } else {
                            if (formDataString != null) {
                                for (String part : parts) {
                                    String[] kv = part.split("=");
                                    if (kv != null) {
                                        if (kv.length > 0) {
                                            existingKeys.remove(kv[0] + ": fp");
                                        }
                                        if (kv.length == 2) {
                                            // TODO how to handle arrays here?
                                            String key = kv[0];
                                            try {
                                                String value = URLDecoder.decode(kv[1], "utf-8");
                                                if (parameter.getName().equals(key)) {
                                                    JavaType jt = parameterClasses[i];
                                                    Class<?> cls = jt.getRawClass();
                                                    try {
                                                        o = validator.convertAndValidate(Arrays.asList(value), parameter, cls, definitions);
                                                    } catch (ConversionException e) {
                                                        missingParams.add(e.getError());
                                                    } catch (ValidationException e) {
                                                        missingParams.add(e.getValidationMessage());
                                                    }
                                                }
                                            } catch (UnsupportedEncodingException e) {
                                                LOGGER.error("unable to decode value for " + key);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
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
                            if ("body".equals(in)) {
                                if (ctx.hasEntity()) {
                                    BodyParameter body = (BodyParameter) parameter;
                                    o = EntityProcessorFactory.readValue(ctx.getMediaType(), ctx.getEntityStream(), cls);
                                    if (o != null) {
                                        validate(o, body.getSchema(), SchemaValidator.Direction.INPUT);
                                    }
                                } else if (parameter.getRequired()) {
                                    ValidationException e = new ValidationException();
                                    e.message(new ValidationMessage()
                                            .message("The input body `" + paramName + "` is required"));
                                    throw e;
                                }
                            }
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
                                io.swagger.models.Response responseSchema = operation.getResponses().get(responseCode);
                                if (responseSchema == null) {
                                    // try default response schema
                                    responseSchema = operation.getResponses().get("default");
                                }
                                if (responseSchema != null && responseSchema.getSchema() != null) {
                                    validate(wrapper.getEntity(), responseSchema.getSchema(), SchemaValidator.Direction.OUTPUT);
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
            Map<String, io.swagger.models.Response> responses = operation.getResponses();
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
                    io.swagger.models.Response response = responses.get(defaultKey);

                    if(response.getHeaders() != null && response.getHeaders().size() > 0) {
                        for(String key: response.getHeaders().keySet()) {
                            Property headerProperty = response.getHeaders().get(key);
                            Object output = ExampleBuilder.fromProperty(headerProperty, definitions);
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

                    Map<String, Object> examples = response.getExamples();
                    if (examples != null) {
                        for (MediaType mediaType : requestContext.getAcceptableMediaTypes()) {
                            for (String key : examples.keySet()) {
                                if (MediaType.valueOf(key).isCompatible(mediaType)) {
                                    builder.entity(examples.get(key))
                                            .type(mediaType);

                                    return builder.build();
                                }
                            }
                        }
                    }

                    Object output = ExampleBuilder.fromProperty(response.getSchema(), definitions);

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

    public void validate(Object o, Property property, SchemaValidator.Direction direction) throws ApiException {
        doValidation(o, property, direction);
    }

    public void validate(Object o, Model model, SchemaValidator.Direction direction) throws ApiException {
        doValidation(o, model, direction);
    }

    public void setContentType(RequestContext res, ResponseContext resp, Operation operation) {
        // honor what has been set, it may be determined by business logic in the controller
        if (resp.getContentType() != null) {
            return;
        }
        List<String> available = operation.getProduces();
        if (available != null) {
            for (String a : available) {
                MediaType mt = MediaType.valueOf(a);
                for (MediaType acceptable : res.getAcceptableMediaTypes()) {
                    if (mt.isCompatible(acceptable)) {
                        resp.setContentType(mt);
                        return;
                    }
                }
            }
            if (available.size() > 0) {
                resp.setContentType(MediaType.valueOf(available.get(0)));
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
