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

package io.swagger.test.schema;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.schema.SchemaValidator;
import io.swagger.util.Json;
import org.junit.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ComplexSchemaValidationTest {

    @Test
    public void testComplexValidPayload() throws Exception {
        Configuration config = new Configuration();
        config.setSwaggerUrl( "http://petstore.swagger.io/v2/swagger.json");

        SchemaValidator validator = new SchemaValidator( config );

        JsonNode response = Json.mapper().readTree(
            "      [{\n" +
            "      \"id\": 1500,\n" +
            "      \"category\":       {\n" +
            "         \"id\": 0,\n" +
            "         \"name\": \"\"\n" +
            "      },\n" +
            "      \"name\": \"butch\",\n" +
            "      \"photoUrls\": [\"\"],\n" +
            "      \"tags\": [      {\n" +
            "         \"id\": 0,\n" +
            "         \"name\": \"\"\n" +
            "      }],\n" +
            "      \"status\": \"available\"\n" +
            "   }" +
            "]");


        String schema = "{\n" +
            "              \"type\": \"array\",\n" +
            "              \"items\": {\n" +
            "                \"$ref\": \"#/definitions/Pet\"\n" +
            "              }\n" +
            "            }";


        assertTrue(validator.validate( response, schema, SchemaValidator.Direction.OUTPUT ));
    }

    @Test
    public void testComplexInvalidPayload() throws Exception {
        Configuration config = new Configuration();
        config.setSwaggerUrl( "http://petstore.swagger.io/v2/swagger.json");

        SchemaValidator validator = new SchemaValidator( config );

        JsonNode response = Json.mapper().readTree(
            "      [{\n" +
                "      \"id\": 1500,\n" +
                "      \"category\":       {\n" +
                "         \"id\": 0,\n" +
                "         \"name\": \"\"\n" +
                "      },\n" +
                "      \"namase\": \"butch\",\n" +
                "      \"phosadtoUrls\": [\"\"],\n" +
                "      \"tags\": [      {\n" +
                "         \"idsd\": 0,\n" +
                "         \"name\": \"\"\n" +
                "      }],\n" +
                "      \"status\": \"available\"\n" +
                "   }" +
                "]");


        String schema = "{\n" +
            "              \"type\": \"array\",\n" +
            "              \"items\": {\n" +
            "                \"$ref\": \"#/definitions/Pet\"\n" +
            "              }\n" +
            "            }";


        assertFalse(validator.validate( response, schema, SchemaValidator.Direction.OUTPUT ));
    }

}
