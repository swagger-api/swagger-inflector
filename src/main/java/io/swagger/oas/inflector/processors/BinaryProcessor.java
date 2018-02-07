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

package io.swagger.oas.inflector.processors;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.io.Files;
import io.swagger.oas.inflector.controllers.OpenAPIOperationController;
import io.swagger.oas.inflector.converters.ConversionException;
import io.swagger.oas.inflector.validators.ValidationError;
import io.swagger.oas.inflector.validators.ValidationException;
import io.swagger.oas.inflector.validators.ValidationMessage;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BinaryProcessor implements EntityProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryProcessor.class);
    private static List<MediaType> SUPPORTED_TYPES = new ArrayList<>();


    static {
        SUPPORTED_TYPES.add(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        SUPPORTED_TYPES.add(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        SUPPORTED_TYPES.add(MediaType.MULTIPART_FORM_DATA_TYPE);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return new ArrayList(SUPPORTED_TYPES);
    }

    @Override
    public void enableType(MediaType type) {
        MediaType t = type;
        if(t != null) {
            if(!SUPPORTED_TYPES.contains(t)) {
                SUPPORTED_TYPES.add(type);
            }
        }
    }

    @Override
    public boolean supports(MediaType mediaType) {
        return SUPPORTED_TYPES.contains(mediaType);
    }


    @Override
    public Object process(MediaType mediaType, InputStream entityStream, Class<?> cls) throws ConversionException {
        try {
            return null;
        } catch (Exception e) {
            LOGGER.trace("unable to extract entity from content-type `" + mediaType, e);
            throw new ConversionException()
                    .message(new ValidationMessage()
                            .code(ValidationError.UNACCEPTABLE_VALUE)
                            .message("unable to convert input to " + cls.getCanonicalName()));
        }
    }

    @Override
    public  Object process(MediaType mediaType, InputStream entityStream, Class<?> cls, OpenAPIOperationController controller) throws ConversionException {
        Object argument = null;
        Map<String, String> headers = new HashMap<>();
        String name = null;
        Map<String, Map<String, String>> formMap = new HashMap<>();
        Map<String, File> inputStreams = new HashMap<>();
        List<ValidationMessage> missingParams = new ArrayList<>();
        Object[] args = new Object[controller.getParameterClasses().length];
        try {
            if (mediaType.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {

                //TODO validate also here what javatype its expected as parameter
                JavaType[] parameters = controller.getParameterClasses();
                for (int i = 0; i < parameters.length; i++) {
                    //validate if its File, byte[] or inputStream and change it to the implemented method
                    if (parameters[i].getRawClass().equals(InputStream.class)){


                    }else if (parameters[i].getRawClass().equals(File.class)) {

                    }else if (parameters[i].getRawClass().equals(byte[].class)){
                        
                        argument = IOUtils.toByteArray(entityStream);

                    }
                }


                return argument;

            }else if (mediaType.isCompatible(MediaType.MULTIPART_FORM_DATA_TYPE)){
                int i = 1;
                // get the boundary
                String boundary = mediaType.getParameters().get("boundary");

                if (boundary != null) {
                    try {
                        InputStream inputStream = entityStream;//ctx.getEntityStream();

                        MultipartStream multipartStream = new MultipartStream(inputStream, boundary.getBytes());
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
                            String filename = extractFilenameFromHeaders(headers);
                            if (filename != null) {
                                try {
                                    File file = new File(Files.createTempDir(), filename);
                                    file.deleteOnExit();
                                    file.getParentFile().deleteOnExit();
                                    FileOutputStream fo = new FileOutputStream(file);
                                    multipartStream.readBodyData(fo);
                                    inputStreams.put(name, file);
                                } catch (Exception e) {
                                    LOGGER.error("Failed to extract uploaded file", e);
                                }
                            } else {
                                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                                multipartStream.readBodyData(bo);
                                String value = bo.toString();
                                headers.put(name, value);
                            }
                            if (name != null) {
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
                try {
                    if (controller.getOperation().getRequestBody().getContent() != null) {

                        //for (String mediaTypeKey : controller.getOperation().getRequestBody().getContent().keySet()) {
                            io.swagger.v3.oas.models.media.MediaType media = controller.getOperation().getRequestBody().getContent().get(MediaType.MULTIPART_FORM_DATA);
                            if (media.getSchema() != null ) {
                                Schema schema = media.getSchema();
                                // look in the form map
                                if (schema.getProperties() != null) {
                                    Map<String, Schema> properties = schema.getProperties();
                                    for (String key : properties.keySet()) {
                                        headers = formMap.get(key);
                                        if (headers != null && headers.size() > 0) {

                                            if ("binary".equals(properties.get(key).getFormat())) {
                                                argument = inputStreams.get(key);

                                            } else {
                                                Object obj = headers.get(key);

                                                if (obj != null) {
                                                    JavaType jt = controller.getParameterClasses()[i];
                                                    cls = jt.getRawClass();

                                                    List<String> stringHeaders = Arrays.asList(obj.toString());
                                                    try {
                                                        argument = controller.getValidator().convertAndValidate(stringHeaders, controller.getOperation().getRequestBody(), cls, controller.getDefinitions());
                                                    } catch (ConversionException e) {
                                                        missingParams.add(e.getError());
                                                    } catch (ValidationException e) {
                                                        missingParams.add(e.getValidationMessage());
                                                    }
                                                }
                                            }
                                        }
                                        args[i] = argument;
                                        argument = null;
                                        i += 1;
                                    }
                                }
                           }
                        //}
                   }
                } catch (NumberFormatException e) {
                    LOGGER.error("Couldn't find body ( ) to " + controller.getParameterClasses()[i], e);
                }
                return args;

            }else if (mediaType.isCompatible(MediaType.APPLICATION_FORM_URLENCODED_TYPE)){
                int i = 1;
                Set<String> existingKeys = new HashSet<>();
                String formDataString = null;
                String[] parts = null;
                try {
                    formDataString = IOUtils.toString(entityStream, "UTF-8");
                    parts = formDataString.split("&");

                    for (String part : parts) {
                        String[] kv = part.split("=");
                        existingKeys.add(kv[0] + ": fp");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //for (String mediaTypeKey : controller.getOperation().getRequestBody().getContent().keySet()) {
                    io.swagger.v3.oas.models.media.MediaType media = controller.getOperation().getRequestBody().getContent().get(mediaType.APPLICATION_FORM_URLENCODED);
                    if (formDataString != null) {
                        if (media.getSchema() != null ) {
                            Schema schema = media.getSchema();
                            if (schema.getProperties() != null) {
                                Map<String, Schema> properties = schema.getProperties();
                                for (String property : properties.keySet()) {
                                    for (String part : parts) {
                                        String[] kv = part.split("=");

                                        if (kv != null) {
                                            if (kv.length > 0) {
                                                existingKeys.remove(kv[0] + ": fp");
                                            }
                                            if (kv.length == 2) {
                                                String key = kv[0];
                                                try {
                                                    String value = URLDecoder.decode(kv[1], "utf-8");
                                                    if (property.equals(key)) {
                                                        JavaType jt = controller.getParameterClasses()[i];
                                                        cls = jt.getRawClass();
                                                        try {
                                                            argument = controller.getValidator().convertAndValidate(Arrays.asList(value), controller.getOperation().getRequestBody(), cls, controller.getDefinitions());
                                                            args[i] = argument;
                                                            argument = null;
                                                            i += 1;
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
                        }
                    }
                //}
                return args;
            }
        } catch (Exception e) {
            LOGGER.trace("unable to extract entity from content-type `" + mediaType, e);
            throw new ConversionException()
                    .message(new ValidationMessage()
                            .code(ValidationError.UNACCEPTABLE_VALUE)
                            .message("unable to convert input to " + cls.getCanonicalName()));
        }
        return null;
    }

    @Override
    public Object process(MediaType mediaType, InputStream entityStream, JavaType javaType) {
        try {
            if (mediaType.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
                return entityStream;
            }else if (mediaType.equals(MediaType.MULTIPART_FORM_DATA_TYPE)){

            }else if (mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE)){

            }
        } catch (Exception e) {
            LOGGER.error("unable to extract entity from content-type `" + mediaType, e);
        }
        return null;
    }

    public static String extractFilenameFromHeaders(Map<String, String> headers) {
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
}
