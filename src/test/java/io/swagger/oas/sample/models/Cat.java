package io.swagger.oas.sample.models;

import javax.xml.bind.annotation.XmlElement;

public class Cat {

    public Cat id(Long id) {
        this.id = id;
        return this;
    }

    public Cat name(String name) {
        this.name = name;
        return this;
    }

    public Cat owner(Owner owner) {
        this.owner = owner;
        return this;
    }

    @XmlElement
    public Long id;

    @XmlElement
    public String name;

    @XmlElement
    public Owner owner;
}