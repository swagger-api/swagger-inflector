package io.swagger.oas.inflector.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.util.Json;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper objectMapper;
    private boolean prettyPrint;

    public JsonProvider() {
        objectMapper = Json.mapper();
    }

    public JsonProvider(boolean prettyPrint) {
        this();
        this.prettyPrint = prettyPrint;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        if(this.prettyPrint) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        return objectMapper;
    }
}