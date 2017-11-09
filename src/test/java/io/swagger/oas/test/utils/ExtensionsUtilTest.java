package io.swagger.oas.test.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.sun.org.apache.xerces.internal.xs.StringList;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.oas.sample.models.Dog;
import io.swagger.oas.inflector.config.Configuration;
import io.swagger.oas.inflector.utils.ExtensionsUtil;

import io.swagger.oas.inflector.utils.ReflectionUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;


import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.util.ResolverFully;
import mockit.Injectable;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class ExtensionsUtilTest {

    @Test
    public void resolveComposedReferenceSchema(@Injectable final List<AuthorizationValue> auths){

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readLocation("./src/test/swagger/oas3.yaml",auths,options).getOpenAPI();
        ResolverFully resolverUtil = new ResolverFully();
        resolverUtil.resolveFully(openAPI);

        assertTrue(openAPI.getPaths().get("/withInvalidComposedModelArray").getPost().getRequestBody().getContent().get("*/*").getSchema() instanceof ArraySchema);
        ArraySchema arraySchema = (ArraySchema) openAPI.getPaths().get("/withInvalidComposedModelArray").getPost().getRequestBody().getContent().get("*/*").getSchema();
        assertTrue(arraySchema.getItems() instanceof ObjectSchema);

    }

    @Test
    public void testArrayParam(@Injectable final List<AuthorizationValue> auths) throws IOException{
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);

        String pathFile = FileUtils.readFileToString(new File("./src/test/swagger/oas3.yaml"),"UTF-8");
        SwaggerParseResult result = new OpenAPIV3Parser().readContents(pathFile, auths, options);
        OpenAPI openAPI = result.getOpenAPI();

        new ExtensionsUtil().addExtensions(openAPI);

        Operation operation = openAPI.getPaths().get("/pet").getPost();
        RequestBody body = operation.getRequestBody();

        assertNotNull(body);
        Schema model = body.getContent().get("application/json").getSchema();
        assertEquals("object", model.getType());
    }

    @Test
    public void testResolveRequestBody(@Injectable final List<AuthorizationValue> auths) throws Exception {
        ReflectionUtils utils = new ReflectionUtils();
        utils.setConfiguration( Configuration.read("src/test/config/config1.yaml"));

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);


        String pathFile = FileUtils.readFileToString(new File("./src/test/swagger/oas3.yaml"),"UTF-8");
        SwaggerParseResult result = new OpenAPIV3Parser().readContents(pathFile, auths, options);
        OpenAPI openAPI = result.getOpenAPI();

        new ExtensionsUtil().addExtensions(openAPI);

        Operation operation = openAPI.getPaths().get("/mappedWithDefinedModel/{id}").getPost();
        RequestBody body = operation.getRequestBody();

        JavaType jt = utils.getTypeFromRequestBody(body, openAPI.getComponents().getSchemas())[0];
        assertEquals(jt.getRawClass(), Dog.class);
    }




    @Test
    public void selfReferenceTest(@Injectable final List<AuthorizationValue> auths) {
        String yaml = "" +
                "openapi: '3.0'\n" +
                "paths:\n" +
                "  /selfRefA:\n" +
                "    get:\n" +
                "      parameters:\n" +
                "        - in: query\n" +
                "          name: body\n" +
                "          schema:\n" +
                "            $ref: '#/components/Schemas/SchemaA'\n" +
                "  /selfRefB:\n" +
                "    get:\n" +
                "      parameters:\n" +
                "        - in: query\n" +
                "          name: body\n" +
                "          schema:\n" +
                "            $ref: '#/components/Schemas/SchemaB'\n" +
                "  /selfRefC:\n" +
                "    get:\n" +
                "      parameters:\n" +
                "        - in: query\n" +
                "          name: body\n" +
                "          schema:\n" +
                "            $ref: '#/components/Schemas/SchemaC'\n" +
                "  /selfRefD:\n" +
                "    get:\n" +
                "      parameters: []\n" +
                "      responses:\n" +
                "           default:\n"+
                "               content:\n"+
                "                'application/json':\n"+
                "                     schema:\n"+
                "                        type: array\n" +
                "                        items:\n" +
                "                           $ref: '#/components/Schemas/SchemaA'\n" +
                "  /selfRefE:\n" +
                "    get:\n" +
                "      parameters: []\n" +
                "      responses:\n" +
                "           default:\n"+
                "               content:\n"+
                "                'application/json':\n"+
                "                     schema:\n"+
                "                        type: array\n" +
                "                        items:\n" +
                "                           $ref: '#/components/Schemas/SchemaA'\n" +

                "components:\n" +
                "   schemas:\n" +
                "       SchemaA:\n" +
                "           properties:\n" +
                "               modelB:\n" +
                "                   $ref: '#/components/Schemas/SchemaB'\n" +
                "       SchemaB:\n" +
                "           properties:\n" +
                "                modelB:\n" +
                "                    $ref: '#/components/Schemas/SchemaB'\n" +
                "       SchemaC:\n" +
                "            properties:\n" +
                "               modelA:\n" +
                "                   $ref: '#/components/Schemas/SchemaA'";

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readContents(yaml,auths,options).getOpenAPI();
        new ExtensionsUtil().addExtensions(openAPI); // TODO write test for these extensions in schema paths an components as well

        Schema schemaB = openAPI.getPaths().get("/selfRefB").getGet().getParameters().get(0).getSchema();
        assertTrue(schemaB instanceof Schema);

        Assert.assertEquals(schemaB, openAPI.getComponents().getSchemas().get("SchemaB"));

        Schema schema = openAPI.getPaths().get("/selfRefE").getGet().getResponses().get("default").getContent().get("application/json").getSchema();
        assertTrue(schema instanceof ArraySchema);
        ArraySchema arraySchema = (ArraySchema) schema;
        Assert.assertEquals(arraySchema.getItems(), openAPI.getComponents().getSchemas().get("SchemaA"));

    }



    @Test
    public void testSelfReferenceResolution(@Injectable final List<AuthorizationValue> auths)throws Exception {

        String yaml = "" +
                "openapi: 3.0.0\n" +
                        "paths:\n" +
                        "  \"/selfRefB\":\n" +
                        "    get:\n" +
                        "      requestBody:\n" +
                        "        description: user to add to the system\\n\"+\n" +
                        "        content:\n" +
                        "         'application/json':\n" +
                        "             schema:\n" +
                        "                $ref: '#/components/schemas/SchemaB'\n" +
                        "components:\n" +
                        "  schemas:\n" +
                        "    SchemaA:\n" +
                        "      properties:\n" +
                        "        name:\n" +
                        "          type: string\n" +
                        "        modelB:\n" +
                        "          $ref: '#/components/schemas/SchemaB'\n" +
                        "    SchemaB:\n" +
                        "      properties:\n" +
                        "        modelB:\n" +
                        "          type: object\n" +
                        "          properties:\n" +
                        "           id:\n" +
                        "             type: integer\n" +
                        "             format: int64\n" +
                        "           name:\n" +
                        "             type: string";

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readContents(yaml,auths,options).getOpenAPI();
        ExtensionsUtil extensionsUtil = new ExtensionsUtil();
        extensionsUtil.addExtensions(openAPI);
        Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
        Assert.assertEquals(schemas.get("SchemaA").getExtensions().get("x-swagger-router-model"),"SchemaA");
        Assert.assertEquals(openAPI.getPaths().get("/selfRefB").getGet().getRequestBody().getContent().get("application/json").getSchema().getExtensions().get("x-swagger-router-model"),"SchemaB");

        RequestBody body = openAPI.getPaths().get("/selfRefB").getGet().getRequestBody();
        Schema schema = body.getContent().get("application/json").getSchema();

        assertEquals(schema,openAPI.getComponents().getSchemas().get("SchemaB"));
    }
}
