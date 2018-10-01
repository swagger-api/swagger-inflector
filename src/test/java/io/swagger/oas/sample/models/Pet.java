/**
 * Copyright 2018 SmartBear Software
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.swagger.oas.sample.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Pet")
public class Pet {
    private Long id;
    private Category category;
    private String name;

    @XmlElement(name = "id")
    public long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @XmlElement(name = "category")
    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
