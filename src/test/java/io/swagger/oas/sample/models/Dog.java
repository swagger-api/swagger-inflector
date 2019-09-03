package io.swagger.oas.sample.models;

import javax.xml.bind.annotation.XmlElement;

public class Dog {

    public Dog id(Long id) {
        this.id = id;
        return this;
    }

    public Dog name(String name) {
        this.name = name;
        return this;
    }

    public Dog dogType(String dogType) {
        this.dogType = dogType;
        return this;
    }

    @XmlElement
    public Long id;

    @XmlElement
    public String name;

    @XmlElement
    public String dogType;
}