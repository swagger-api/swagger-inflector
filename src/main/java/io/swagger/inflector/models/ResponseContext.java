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

package io.swagger.inflector.models;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

public class ResponseContext {
    private MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>();
    private MediaType contentType;
    private int status = Status.OK.getStatusCode();
    private Object entity;

    public ResponseContext header(String key, String value) {
        this.headers.add(key, value);
        return this;
    }

    public ResponseContext contentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    public ResponseContext contentType(String contentType) {
        this.contentType = MediaType.valueOf(contentType);
        return this;
    }

    public ResponseContext status(Status status) {
        this.status = status.getStatusCode();
        return this;
    }

    public ResponseContext status(int status) {
        this.status = status;
        return this;
    }

    public ResponseContext entity(Object entity) {
        this.entity = entity;
        return this;
    }

    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(MultivaluedMap<String, String> headers) {
        this.headers = headers;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }
}
