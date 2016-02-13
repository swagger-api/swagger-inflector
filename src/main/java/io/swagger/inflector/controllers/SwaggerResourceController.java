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
import io.swagger.inflector.utils.VendorSpecFilter;
import io.swagger.models.Swagger;
import org.glassfish.jersey.process.Inflector;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class SwaggerResourceController implements Inflector<ContainerRequestContext, Response> {
    private Swagger swagger;

    public SwaggerResourceController(Swagger swagger) {
        this.swagger = swagger;
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
            return Response.ok().entity(new VendorSpecFilter().filter(swagger, filter, null, cookies, headers)).build();
        }
        return Response.ok().entity(swagger).build();
    }
}
