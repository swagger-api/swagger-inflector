/*
 * Copyright 2017 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.swagger.oas.inflector.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public class DirectionDeserializer extends JsonDeserializer<Set<Configuration.Direction>> {

    @Override
    public Set<Configuration.Direction> deserialize(JsonParser jp,
            DeserializationContext ctxt) throws IOException {
        final JsonToken token = jp.getCurrentToken();
        if (token == JsonToken.VALUE_FALSE) {
            return EnumSet.noneOf(Configuration.Direction.class);
        } else if (token == JsonToken.VALUE_TRUE) {
            return EnumSet.allOf(Configuration.Direction.class);
        } else if (token == JsonToken.START_ARRAY) {
            final Set<Configuration.Direction> items = EnumSet.noneOf(Configuration.Direction.class);
            while (true) {
                final JsonToken next = jp.nextToken();
                if (next == JsonToken.VALUE_STRING) {
                    final String name = jp.getText();
                    items.add(Configuration.Direction.valueOf(name));
                } else if (next == JsonToken.END_ARRAY) {
                    return items;
                } else {
                    break;
                }
            }
        }
        throw InvalidDefinitionException.from(jp, String.format("Failed to deserialize %s", jp.getText()), ctxt.constructType(Configuration.Direction.class));
    }
}
