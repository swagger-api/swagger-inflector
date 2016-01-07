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

package io.swagger.inflector.processors;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.swagger.inflector.converters.ConversionException;
import io.swagger.inflector.validators.ValidationError;
import io.swagger.inflector.validators.ValidationMessage;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class JacksonProcessor implements EntityProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonProcessor.class);

    private static XmlMapper XML = new XmlMapper();
    private static MediaType APPLICATION_YAML = new MediaType("application", "yaml");
    private static Collection<MediaType> SUPPORTED_TYPES = Collections
            .unmodifiableList(Arrays.asList(MediaType.APPLICATION_JSON_TYPE,
                    MediaType.APPLICATION_XML_TYPE, APPLICATION_YAML, MediaType.TEXT_PLAIN_TYPE));

    @Override
    public boolean supports(MediaType mediaType) {
        for (MediaType item : SUPPORTED_TYPES) {
            if (item.isCompatible(mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object process(MediaType mediaType, InputStream entityStream,
                          JavaType javaType) {
        try {
            if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
                return Json.mapper().readValue(entityStream, javaType);
            }
            if (MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)) {
                return XML.readValue(entityStream, javaType);
            }
            if (APPLICATION_YAML.isCompatible(mediaType)) {
                return Yaml.mapper().readValue(entityStream, javaType);
            }
        } catch (IOException e) {
            LOGGER.error("unable to extract entity from content-type `" + mediaType + "` to " + javaType.toCanonical(), e);
        }

        return null;
    }

    @Override
    public Object process(MediaType mediaType, InputStream entityStream, Class<?> cls) throws ConversionException {
        try {
            if(String.class.equals(cls)) {
                OutputStream outputStream = new ByteArrayOutputStream();
                IOUtils.copy(entityStream, outputStream);
                return outputStream.toString();
            }
            if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
                return Json.mapper().readValue(entityStream, cls);
            }
            if (MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)) {
                return XML.readValue(entityStream, cls);
            }
            if (APPLICATION_YAML.isCompatible(mediaType)) {
                return Yaml.mapper().readValue(entityStream, cls);
            }
        } catch (Exception e) {
            LOGGER.trace("unable to extract entity from content-type `" + mediaType + "` to " + cls.getCanonicalName(), e);
            throw new ConversionException()
                    .message(new ValidationMessage()
                            .code(ValidationError.UNACCEPTABLE_VALUE)
                            .message("unable to convert input to " + cls.getCanonicalName()));
        }

        return null;
    }
}