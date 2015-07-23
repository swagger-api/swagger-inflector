package io.swagger.inflector.models;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response.Status;

public class ResponseWrapper {
  MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>();
  MediaType contentType;
  Status status;
  Object entity;

  public ResponseWrapper header(String key, String value) {
    this.headers.add(key, value);
    return this;
  }
  public ResponseWrapper contentType(MediaType contentType) {
    this.contentType = contentType;
    return this;
  }
  public ResponseWrapper status(Status status) {
    this.status = status;
    return this;
  }
  public ResponseWrapper entity(Object entity) {
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

  public Status getStatus() {
    return status;
  }
  public void setStatus(Status status) {
    this.status = status;
  }

  public Object getEntity() {
    return entity;
  }
  public void setEntity(Object entity) {
    this.entity = entity;
  }
}
