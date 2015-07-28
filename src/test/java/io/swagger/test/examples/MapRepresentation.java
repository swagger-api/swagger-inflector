/*
 *  Copyright 2015 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.swagger.test.examples;

import io.swagger.inflector.examples.models.AbstractExample;
import io.swagger.inflector.examples.models.Example;

import java.util.ArrayList;
import java.util.List;

public class MapRepresentation extends AbstractExample {
    List<Example> values = null;

    public void add(Example value) {
        if (values == null) {
            values = new ArrayList<Example>();
        }
        values.add(value);
    }

    @Override
    public String asString() {
        return "NOT IMPLEMENTED";
    }

    public List<Example> getAdditionalProperties() {
        if (values == null) {
            return new ArrayList<Example>();
        }
        return values;
    }
}
