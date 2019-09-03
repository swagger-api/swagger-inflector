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

import io.swagger.oas.inflector.controllers.OpenAPIOperationController;
import io.swagger.oas.inflector.converters.ConversionException;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EntityProcessorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityProcessorFactory.class);

    private static List<EntityProcessor> PROCESSORS = new ArrayList<EntityProcessor>();

    static {
        // handles yaml, json, xml
        PROCESSORS.add(new JacksonProcessor());
        PROCESSORS.add(new BinaryProcessor());
        PROCESSORS.add(new PlainProcessor());
    }

    public static void addProcessor(Class<?> cls, MediaType type) {
        for(EntityProcessor entityProcessor : PROCESSORS) {
            if(entityProcessor.getClass().equals(cls)) {
                entityProcessor.enableType(type);
                return;
            }
        }
        try {
            EntityProcessor processor = (EntityProcessor) cls.newInstance();
            PROCESSORS.add(processor);
            processor.enableType(type);
        }
        catch (Exception e) {
            LOGGER.debug("unable to add processor " + cls.getName());
        }
    }

    public static Object readValue(MediaType mediaType, InputStream entityStream, Class<?> class1) throws ConversionException {
        for (EntityProcessor p : getProcessors()) {
            if (p.supports(mediaType)) {
                return p.process(mediaType, entityStream, class1);
            }
        }
        return null;
    }

    public static Object readValue(MediaType mediaType, InputStream entityStream, Class<?> class1, OpenAPIOperationController controller) throws ConversionException {
        for (EntityProcessor p : getProcessors()) {
            if (p.supports(mediaType)) {
                return p.process(mediaType, entityStream, class1, controller);
            }
        }
        return null;
    }

    public static List<EntityProcessor> getProcessors() {
        return PROCESSORS;
    }
}
