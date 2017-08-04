package io.swagger.test.utils;

//import io.swagger.oas.inflector.config.Configuration;
import io.swagger.oas.inflector.utils.ExtensionsUtil;

//import io.swagger.oas.inflector.utils.ReflectionUtils;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.Operation;


import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.Schema;

import io.swagger.oas.models.parameters.RequestBody;
import io.swagger.parser.models.AuthorizationValue;
import io.swagger.parser.models.ParseOptions;
import io.swagger.parser.models.SwaggerParseResult;
import io.swagger.parser.v3.OpenAPIV3Parser;

import io.swagger.util.Json;

import mockit.Injectable;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

public class ExtensionsUtilTest {
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
        assertEquals(model.getType(),"object");
    }

    /*@Test
    public void testResolveBodyParam(@Injectable final List<AuthorizationValue> auths, @Injectable final ParseOptions options) throws Exception {
        ReflectionUtils utils = new ReflectionUtils();
        utils.setConfiguration( Configuration.read("src/test/config/config1.yaml"));

        String pathFile = FileUtils.readFileToString(new File("./src/test/swagger/sample1.yaml"),"UTF-8");
        OpenAPI openAPI = new OpenAPIV3Parser().readContents(pathFile,);
        new ResolverUtil().resolveFully(openAPI);

        Operation operation = openAPI.getPaths().get("/mappedWithDefinedModel/{id}").getPost();
        Parameter param = operation.getParameters().get(1);

        JavaType jt = utils.getTypeFromParameter(param, openAPI.getComponents().getSchemas());
        assertEquals(jt.getRawClass(), Dog.class);
    }*/

    /*@Test
    public void testIssue85() {
        String yaml =
                "swagger: '2.0'\n" +
                "paths: \n" +
                "  /test/method: \n" +
                "    post: \n" +
                "      parameters: \n" +
                "        - \n" +
                "          in: \"body\"\n" +
                "          name: \"body\"\n" +
                "          required: false\n" +
                "          schema: \n" +
                "            $ref: '#/definitions/StructureA'\n" +
                "definitions: \n" +
                "  StructureA: \n" +
                "    type: object\n" +
                "    properties: \n" +
                "      someProperty: \n" +
                "        type: string\n" +
                "      arrayOfOtherType: \n" +
                "        type: array\n" +
                "        items: \n" +
                "          $ref: '#/definitions/StructureB'\n" +
                "  StructureB: \n" +
                "    type: object\n" +
                "    properties: \n" +
                "      someProperty: \n" +
                "        type: string\n";

        OpenAPI openAPI = new OpenAPIV3Parser().parse(yaml);
        new ResolverUtil().resolveFully(openAPI);

        Parameter param = openAPI.getPaths().get("/test/method").getPost().getParameters().get(0);
        assertTrue(param instanceof BodyParameter);
        BodyParameter bp = (BodyParameter) param;
        Schema schema = bp.getSchema();

        assertTrue(schema instanceof ModelImpl);
        assertNotNull(schema.getProperties().get("someProperty"));

        ArrayProperty am = (ArrayProperty) schema.getProperties().get("arrayOfOtherType");
        assertNotNull(am);
        Property prop = am.getItems();
        assertTrue(prop instanceof ObjectProperty);
    }*/

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
