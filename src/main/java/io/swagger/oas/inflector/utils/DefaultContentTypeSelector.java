/*
 *  Copyright 2006 SmartBear Software
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

package io.swagger.oas.inflector.utils;

import io.swagger.oas.inflector.CustomMediaTypes;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DefaultContentTypeSelector implements ContentTypeSelector {
    private final MediaType type;
    private final Set<String> overriden;

    public DefaultContentTypeSelector(MediaType type) {
        this(type, CustomMediaTypes.APPLICATION_YAML.getSubtype());
    }

    public DefaultContentTypeSelector(MediaType type, String... overriden) {
        this.type = type;
        final Set<String> tmp = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        tmp.add(MediaType.MEDIA_TYPE_WILDCARD);
        tmp.addAll(Arrays.asList(overriden));
        this.overriden = Collections.unmodifiableSet(tmp);
    }

    @Override
    public boolean apply(List<MediaType> acceptable, Response.ResponseBuilder builder) {
        for (MediaType item : acceptable) {
            if (!overriden.contains(item.getSubtype())) {
                return false;
            }
        }
        builder.type(type);
        return true;
    }
}
