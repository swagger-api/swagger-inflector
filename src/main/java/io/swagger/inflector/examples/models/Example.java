package io.swagger.inflector.examples.models;

import javax.xml.bind.annotation.XmlAttribute;

public interface Example {
  String getName();
  void setName(String name);
  
  @XmlAttribute
  String getNamespace();
  void setNamespace(String namespace);
  
  @XmlAttribute
  String getPrefix();
  void setPrefix(String prefix);
  
  Boolean getAttribute();
  void setAttribute(Boolean attribute);
  
  Boolean getWrapped();
  void setWrapped(Boolean wrapped);

  String getWrappedName();
  void setWrappedName(String name);

  String asString();
}
