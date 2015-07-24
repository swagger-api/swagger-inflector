package io.swagger.inflector.models;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response.Status;

public class ResponseContext {
  MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>();
  MediaType contentType;
  Status status;
  Object entity;

  public ResponseContext header(String key, String value) {
    this.headers.add(key, value);
    return this;
  }
  public ResponseContext contentType(MediaType contentType) {
    this.contentType = contentType;
    return this;
  }
  public ResponseContext status(Status status) {
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
