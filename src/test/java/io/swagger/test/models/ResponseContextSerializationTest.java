package io.swagger.test.models;

import io.swagger.inflector.models.ResponseContext;
import io.swagger.util.Json;
import org.testng.annotations.Test;

public class ResponseContextSerializationTest {
    @Test
    public void testHeader() {
        ResponseContext ctx = new ResponseContext()
                .header("foo", "bar");

        Json.prettyPrint(ctx);

        for (String key : ctx.getHeaders().keySet()) {
            System.out.println(key);
            System.out.println(ctx.getHeaders().get(key));
        }
    }
}
