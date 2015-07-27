package io.swagger.inflector.examples.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractExample implements Example {
  private String name = null;
  private String namespace = null;
  private String prefix = null;
  private Boolean attribute = false;
  private Boolean wrapped = false;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  @JsonIgnore
  public String getNamespace() {
    return namespace;
  }
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @JsonIgnore
  public String getPrefix() {
    return prefix;
  }
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  @JsonIgnore
  public Boolean getAttribute() {
    return attribute;
  }
  public void setAttribute(Boolean attribute) {
    this.attribute = attribute;
  }

  @JsonIgnore
  public Boolean getWrapped() {
    return wrapped;
  }
  public void setWrapped(Boolean wrapped) {
    this.wrapped = wrapped;
  }
}
