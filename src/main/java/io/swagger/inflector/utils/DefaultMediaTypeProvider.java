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

package io.swagger.inflector.utils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;

public class DefaultMediaTypeProvider implements ContextResolver<MediaType> {
    private final MediaType mediaType;

    public DefaultMediaTypeProvider(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public MediaType getContext(Class<?> type) {
        return mediaType;
    }
}
