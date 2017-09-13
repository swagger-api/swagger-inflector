package io.swagger.oas.sample.models;

import javax.xml.bind.annotation.XmlElement;

public class Dog {
    @XmlElement
    public Long id;

    @XmlElement
    public String name;

    @XmlElement
    public String dogType;
}
