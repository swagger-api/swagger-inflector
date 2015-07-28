package io.swagger.inflector.examples.models;

public abstract class AbstractExample implements Example {
  private String name = null;
  private String namespace = null;
  private String prefix = null;
  private Boolean attribute = false;
  private Boolean wrapped = false;
  private String wrappedName = null;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getNamespace() {
    return namespace;
  }
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getPrefix() {
    return prefix;
  }
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public Boolean getAttribute() {
    return attribute;
  }
  public void setAttribute(Boolean attribute) {
    this.attribute = attribute;
  }

  public Boolean getWrapped() {
    return wrapped;
  }
  public void setWrapped(Boolean wrapped) {
    this.wrapped = wrapped;
  }

  public String getWrappedName() {
    return wrappedName;
  }
  public void setWrappedName(String wrappedName) {
    this.wrappedName = wrappedName;
  }
}
