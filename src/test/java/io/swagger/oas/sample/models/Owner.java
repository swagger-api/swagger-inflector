package io.swagger.oas.sample.models;

import javax.xml.bind.annotation.XmlElement;

public class Owner {

    public Owner id(Long id) {
        this.id = id;
        return this;
    }

    public Owner name(String name) {
        this.name = name;
        return this;
    }

    @XmlElement
    public Long id;

    @XmlElement
    public String name;

}