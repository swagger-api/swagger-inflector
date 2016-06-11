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

import io.swagger.config.FilterFactory;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.inflector.config.SwaggerProcessor;
import io.swagger.inflector.utils.VendorSpecFilter;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class SwaggerResourceController implements Inflector<ContainerRequestContext, Response> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerResourceController.class);

    private Swagger swagger;
    private List<SwaggerProcessor> swaggerProcessors;

    public SwaggerResourceController(Swagger swagger, List<String> swaggerProcessors) {
        this.swagger = swagger;

        this.swaggerProcessors = new ArrayList<>(swaggerProcessors.size());
        for (String swaggerProcessorClass : swaggerProcessors) {
            try {
                this.swaggerProcessors.add(((SwaggerProcessor) SwaggerResourceController.class.getClassLoader()
                        .loadClass(swaggerProcessorClass).newInstance()));
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                LOGGER.error("Unable to load class: " + swaggerProcessorClass, e);
            }
        }
    }

    @Override
    public Response apply(ContainerRequestContext arg0) {
        SwaggerSpecFilter filter = FilterFactory.getFilter();
        if(filter != null) {
            // Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers
            Map<String, Cookie> cookiesvalue = arg0.getCookies();
            Map<String, String> cookies = new HashMap<String, String>();
            if(cookiesvalue != null) {
                for(String key: cookiesvalue.keySet()) {
                    cookies.put(key, cookiesvalue.get(key).getValue());
                }
            }

            MultivaluedMap<String, String> headers = arg0.getHeaders();
            return Response.ok().entity(new VendorSpecFilter().filter(getSwagger(), filter, null, cookies, headers)).build();
        }
        return Response.ok().entity(getSwagger()).build();
    }

    private Swagger getSwagger() {
        if (!swaggerProcessors.isEmpty()) {
            try {
                final Swagger swagger = Json.mapper().readValue(Json.mapper().writeValueAsString(this.swagger),
                        Swagger.class);
                for (SwaggerProcessor swaggerProcessor : swaggerProcessors) {
                    swaggerProcessor.process(swagger);
                }
                return swagger;
            } catch (IOException e) {
                LOGGER.error("Unable to serialize/deserialize swagger: " + swagger, e);
            }
        }
        return swagger;
    }
}
