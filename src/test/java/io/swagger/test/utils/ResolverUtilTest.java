package io.swagger.test.utils;

import io.swagger.oas.inflector.utils.ResolverUtil;

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

public class ResolverUtilTest {
    @Test
    public void testArrayParam(@Injectable final List<AuthorizationValue> auths, @Injectable final ParseOptions options) throws IOException{

        String pathFile = FileUtils.readFileToString(new File("./src/test/swagger/sample1.yaml"),"UTF-8");
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(pathFile, auths, options);
        OpenAPI openAPI = result.getOpenAPI();

        new ResolverUtil().resolveFully(openAPI);
        Operation operation = openAPI.getPaths().get("/withModelArray/{id}").getPost();
        RequestBody body = operation.getRequestBody();

        Json.prettyPrint(openAPI);

        assertNotNull(body);
        Schema model = body.getContent().get("************").getSchema();
        assertTrue(model instanceof ArraySchema);
        //assertTrue(((ArraySchema)model).getItems());
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
    public void selfReferenceTest(@Injectable final List<AuthorizationValue> auths, @Injectable final ParseOptions options) {
        //TODO change the yaml to 3.0
        String yaml = "" +
                "swagger: '2.0'\n" +
                "paths:\n" +
                "  /selfRefA:\n" +
                "    get:\n" +
                "      parameters:\n" +
                "        - in: body\n" +
                "          name: body\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/ModelA'\n" +
                "  /selfRefB:\n" +
                "    get:\n" +
                "      parameters:\n" +
                "        - in: body\n" +
                "          name: body\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/ModelB'\n" +
                "  /selfRefC:\n" +
                "    get:\n" +
                "      parameters:\n" +
                "        - in: body\n" +
                "          name: body\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/ModelC'\n" +
                "  /selfRefD:\n" +
                "    get:\n" +
                "      parameters: []\n" +
                "      responses:\n" +
                "        default:\n" +
                "          schema:\n" +
                "            $ref: '#/definitions/ModelA'\n" +
                "  /selfRefE:\n" +
                "    get:\n" +
                "      parameters: []\n" +
                "      responses:\n" +
                "        default:\n" +
                "          schema:\n" +
                "            type: array\n" +
                "            items:\n" +
                "              $ref: '#/definitions/ModelA'\n" +
                "definitions:\n" +
                "  ModelA:\n" +
                "    properties:\n" +
                "      modelB:\n" +
                "        $ref: '#/definitions/ModelB'\n" +
                "  ModelB:\n" +
                "    properties:\n" +
                "      modelB:\n" +
                "        $ref: '#/definitions/ModelB'\n" +
                "  ModelC:\n" +
                "    properties:\n" +
                "      modelA:\n" +
                "        $ref: '#/definitions/ModelA'";

        options.setResolve(true);

        OpenAPI openAPI = new OpenAPIV3Parser().readContents(yaml,auths,options).getOpenAPI();

        new ResolverUtil().resolveFully(openAPI);
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

        OpenAPI openAPI = new OpenAPIV3Parser().readContents(yaml,auths,options).getOpenAPI();
        ResolverUtil resolverUtil = new ResolverUtil();
        resolverUtil.resolveFully(openAPI);
        Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
        Assert.assertEquals(schemas.get("SchemaA").getExtensions().get("x-swagger-router-model"),"SchemaA");

        RequestBody body = openAPI.getPaths().get("/selfRefB").getGet().getRequestBody();
        Schema schema = body.getContent().get("application/json").getSchema();

        assertEquals(schema,openAPI.getComponents().getSchemas().get("SchemaB"));
    }
}
