package io.swagger.inflector.models;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class RequestContext {
  MultivaluedMap<String, String> headers;
  MediaType mediaType;
  List<MediaType> acceptableMediaTypes;

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
}
