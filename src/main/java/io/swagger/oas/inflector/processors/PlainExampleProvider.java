package io.swagger.oas.inflector.processors;


import io.swagger.oas.inflector.examples.models.Example;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
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

