package io.swagger.test.models;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="user")
public class User {
  private Long id;
  private String user;
  private List<String> names;

  public User id(Long id) {
    this.id = id;
    return this;
  }
  public User user(String user) {
    this.user = user;
    return this;
  }
  
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }

  @XmlElementWrapper(name = "children")
  @XmlElement(name = "child")
  public List<String> getChildNames() {
    return names;
  }
  public void setChildNames(List<String> names) {
    this.names = names;
  }
}