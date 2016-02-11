package io.swagger.test.utils;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.utils.ReflectionUtils;
import io.swagger.inflector.utils.ResolverUtil;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.sample.models.Dog;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class ResolverUtilTest {
    @Test
    public void testArrayParam() {
        Swagger swagger = new SwaggerParser().read("./src/test/swagger/sample1.yaml");

        new ResolverUtil().resolveFully(swagger);
        Operation operation = swagger.getPath("/withModelArray/{id}").getPost();
        Parameter param = operation.getParameters().get(1);

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
}
