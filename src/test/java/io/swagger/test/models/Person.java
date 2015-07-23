package io.swagger.test.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Person {
  @XmlElement
  public Long id;
  
  @XmlElement
  public String name;
}
