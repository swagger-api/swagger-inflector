/*
 *  Copyright 2016 SmartBear Software
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

package io.swagger.inflector;

public class Constants {
    public static final String X_SWAGGER_ROUTER_CONTROLLER = VendorExtension.X_SWAGGER_ROUTER_CONTROLLER.getValue();
    public static final String X_SWAGGER_ROUTER_MODEL = VendorExtension.X_SWAGGER_ROUTER_MODEL.getValue();
    public static final String X_INFLECTOR_HIDDEN = "x-inflector-hidden";

    public enum VendorExtension {
        X_SWAGGER_ROUTER_CONTROLLER("x-swagger-router-controller"),
        X_SWAGGER_ROUTER_MODEL("x-swagger-router-model");

        private String value;

        VendorExtension(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }
}
