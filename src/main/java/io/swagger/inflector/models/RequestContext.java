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

package io.swagger.inflector.models;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

public class RequestContext {
    ContainerRequestContext context;
    MultivaluedMap<String, String> headers;
    MediaType mediaType;
    List<MediaType> acceptableMediaTypes;

    public RequestContext() {}

    public RequestContext(ContainerRequestContext ctx) {
        this.context = ctx;
        if(ctx != null) {
            headers(ctx.getHeaders());
            mediaType(ctx.getMediaType());
            acceptableMediaTypes(ctx.getAcceptableMediaTypes());
        }
    }

    public RequestContext headers(MultivaluedMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public RequestContext mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public RequestContext acceptableMediaTypes(List<MediaType> acceptableMediaTypes) {
        this.acceptableMediaTypes = acceptableMediaTypes;
        return this;
    }

    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(MultivaluedMap<String, String> headers) {
        this.headers = headers;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public List<MediaType> getAcceptableMediaTypes() {
        return acceptableMediaTypes;
    }

    public void setAcceptableMediaTypes(List<MediaType> acceptableMediaTypes) {
        this.acceptableMediaTypes = acceptableMediaTypes;
    }

    public ContainerRequestContext getContext() {
        return context;
    }

    public void setContext(ContainerRequestContext context) {
        this.context = context;
    }
}
