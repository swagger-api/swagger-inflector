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

package io.swagger.oas.test;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.oas.inflector.config.Configuration;
import io.swagger.oas.inflector.utils.ReflectionUtils;


import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;


import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.oas.test.models.Person;
import io.swagger.oas.test.models.User;
import org.junit.Before;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class BodyParamExtractionTest {
    ReflectionUtils utils = new ReflectionUtils();

    @BeforeClass
    @Before
    public void setup() {
        Configuration config = new Configuration();
        config.setModelPackage("io.swagger.oas.test.models");
        config.addModelMapping("User", User.class);

        utils.setConfiguration(config);
    }

    @Test
    public void testStringBodyParam() throws Exception {
        Map<String, Schema> definitions = new HashMap<String, Schema>();

        RequestBody body = new RequestBody().
                content(new Content()
                        .addMediaType("application/json",new MediaType()
                            .schema(new Schema().type("string"))));

        JavaType jt = utils.getTypeFromRequestBody(body, "application/json" ,definitions)[0];

        assertEquals(jt.getRawClass(), String.class);
    }

    @Test
    public void testUUIDBodyParam() throws Exception {
        Map<String, Schema> definitions = new HashMap<>();

        RequestBody body = new RequestBody().
                content(new Content()
                        .addMediaType("application/json",new MediaType()
                            .schema(new Schema().type("string").format("uuid"))));

        JavaType jt = utils.getTypeFromRequestBody(body,"application/json" , definitions)[0];

        assertEquals(jt.getRawClass(), UUID.class);
    }

    @Test
    public void testConvertComplexBodyParamWithConfigMapping() throws Exception {
        Map<String, Schema> definitions = new HashMap<>();

        RequestBody body = new RequestBody().
                content(new Content()
                        .addMediaType("application/json",new MediaType()
                                .schema(new Schema().$ref("#/components/schema/User"))));

        JavaType jt = utils.getTypeFromRequestBody(body, "application/json" ,definitions)[0];

        assertEquals(jt.getRawClass(), User.class);
    }

    @Test
    public void testConvertComplexBodyParamWithoutConfigMapping() throws Exception {
        Map<String, Schema> definitions = new HashMap<>();

        RequestBody body = new RequestBody().
                content(new Content()
                        .addMediaType("application/json",new MediaType()
                                .schema(new Schema().$ref("#/components/schemas/Person"))));

        JavaType jt = utils.getTypeFromRequestBody(body, "application/json" ,definitions)[0];

        // will look up from the config model package and ref.simpleName of Person
        assertEquals(jt.getRawClass(), Person.class);
    }

    @Test
    public void testConvertComplexArrayBodyParam() throws Exception {
        Map<String, Schema> definitions = ModelConverters.getInstance().read(Person.class);

        RequestBody body = new RequestBody().
                content(new Content()
                        .addMediaType("application/json",new MediaType().schema(new ArraySchema()
                                .items(new Schema().$ref("#/components/schemas/Person")))));


    
        JavaType jt = utils.getTypeFromRequestBody(body, "application/json" ,definitions)[0];
        assertEquals(jt.getRawClass(), Person[].class);
    }

    @Test
    public void testConvertPrimitiveArrayBodyParam() throws Exception {
        Map<String, Schema> definitions = ModelConverters.getInstance().read(Person.class);

        RequestBody body = new RequestBody().
                content(new Content()
                        .addMediaType("application/json",new MediaType().schema(new ArraySchema()
                                .items(new StringSchema()))));


        JavaType jt = utils.getTypeFromRequestBody(body, "application/json" ,definitions)[0];
        assertNotNull(jt);
        assertEquals(jt.getRawClass(), String[].class);
    }

    @Test
    public void testConvertDoubleArrayBodyParam() throws Exception {
        Map<String, Schema> definitions = ModelConverters.getInstance().read(Person.class);

        RequestBody body = new RequestBody().
                content(new Content()
                        .addMediaType("application/json",new MediaType().schema(new ArraySchema()
                                .items(new ArraySchema().items(new StringSchema())))));


        JavaType jt = utils.getTypeFromRequestBody(body, "application/json" ,definitions)[0];
        assertNotNull(jt);

        assertEquals(jt.getRawClass(), List[].class);
        JavaType inner = jt.getContentType();
        assertEquals(inner.getRawClass(), List.class);
        assertEquals(inner.getContentType().getRawClass(), String.class);

    }
}
