/*
 *  Copyright 2017 SmartBear Software
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

package io.swagger.oas.inflector.config;


import io.swagger.v3.oas.models.OpenAPI;

/**
 * Post-processing hook applied to the parsed {@link io.swagger.v3.oas.models.OpenAPI} model
 * before it is used by the inflector.
 *
 * <p>Register implementations via {@link Configuration#setSwaggerProcessors}. Each processor
 * receives the fully resolved model and may mutate it in place — for example to inject
 * server-side extensions, strip sensitive fields, or validate custom constraints.
 */
public interface OpenAPIProcessor {

    /**
     * Called once per application startup with the resolved OpenAPI model.
     *
     * @param openAPI the mutable, fully-resolved OpenAPI model; never {@code null}
     */
    void process(OpenAPI openAPI);

}
