package io.swagger.test.utils;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.inflector.utils.ResolverUtil;
import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.parser.SwaggerParser;
import io.swagger.sample.models.Dog;
import io.swagger.util.Json;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class ResolverUtilTest {
    @Test
    public void testArrayParam() {
        Swagger swagger = new SwaggerParser().read("./src/test/swagger/sample1.yaml");

        new ResolverUtil().resolveFully(swagger);
        Operation operation = swagger.getPath("/withModelArray/{id}").getPost();
        Parameter param = operation.getParameters().get(1);

        Json.prettyPrint(swagger);

        assertTrue(param instanceof BodyParameter);
        BodyParameter body = (BodyParameter) param;
        Model model = body.getSchema();
        assertTrue(model instanceof ArrayModel);
        assertTrue(((ArrayModel)model).getItems() instanceof ObjectProperty);
    }

    @Test
    public void testResolveBodyParam() throws Exception {
        ReflectionUtils utils = new ReflectionUtils();
        utils.setConfiguration( Configuration.read("src/test/config/config1.yaml"));

        Swagger swagger = new SwaggerParser().read("./src/test/swagger/sample1.yaml");
        new ResolverUtil().resolveFully(swagger);

        Operation operation = swagger.getPath("/mappedWithDefinedModel/{id}").getPost();
        Parameter param = operation.getParameters().get(1);

        JavaType jt = utils.getTypeFromParameter(param, swagger.getDefinitions());
        assertEquals(jt.getRawClass(), Dog.class);
    }

    @Test
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

        Swagger swagger = new SwaggerParser().parse(yaml);
        new ResolverUtil().resolveFully(swagger);

        Parameter param = swagger.getPaths().get("/test/method").getPost().getParameters().get(0);
        assertTrue(param instanceof BodyParameter);
        BodyParameter bp = (BodyParameter) param;
        Model schema = bp.getSchema();

        assertTrue(schema instanceof ModelImpl);
        assertNotNull(schema.getProperties().get("someProperty"));

        ArrayProperty am = (ArrayProperty) schema.getProperties().get("arrayOfOtherType");
        assertNotNull(am);
        Property prop = am.getItems();
        assertTrue(prop instanceof ObjectProperty);
    }

    @Test
    public void selfReferenceTest() {
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

        Swagger swagger = new SwaggerParser().parse(yaml);
        new ResolverUtil().resolveFully(swagger);
    }

    @Test
    public void testSelfReferenceResolution() {
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
                "\n" +
                "definitions:\n" +
                "  ModelA:\n" +
                "    properties:\n" +
                "      name:\n" +
                "        type: string\n" +
                "      modelB:\n" +
                "        $ref: '#/definitions/ModelB'\n" +
                "  ModelB:\n" +
                "    properties:\n" +
                "      modelB:\n" +
                "        $ref: '#/definitions/ModelB'";
        Swagger swagger = new SwaggerParser().parse(yaml);
        new ResolverUtil().resolveFully(swagger);
    }
}
