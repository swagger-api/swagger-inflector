package io.swagger.oas.inflector.processors;


import io.swagger.oas.inflector.examples.models.Example;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;



@Provider
@Produces({MediaType.TEXT_PLAIN})
public class PlainExampleProvider extends AbstractExampleProvider implements MessageBodyWriter<Example> {
    @Override
    public void writeTo(Example data,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> headers,
                        OutputStream out) throws IOException {
        if (mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE)) {
            out.write(data.asString().getBytes("utf-8"));
        }
    }
}

