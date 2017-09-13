package io.swagger.oas.inflector.utils;

import io.swagger.oas.inflector.Constants;

import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.callbacks.Callback;
import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.ComposedSchema;
import io.swagger.oas.models.media.MediaType;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ExtensionsUtil {

    private Map<String, Schema> schemas;

    public void addExtensions(OpenAPI openAPI) {
        if (openAPI.getComponents() != null) {
            if (openAPI.getComponents().getSchemas() != null) {
                schemas = openAPI.getComponents().getSchemas();
                if (schemas == null) {
                    schemas = new HashMap<>();
                }
            }

            for (String name : schemas.keySet()) {
                Schema schema = schemas.get(name);
                if (schema.getExtensions() != null) {
                    if (!schema.getExtensions().containsKey(Constants.X_SWAGGER_ROUTER_MODEL)) {
                        schema.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, name);
                    }
                } else {
                    schema.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, name);
                }
            }
        }
        if (openAPI.getPaths() != null) {
            for (String pathname : openAPI.getPaths().keySet()) {
                PathItem pathItem = openAPI.getPaths().get(pathname);
                resolvePath(pathItem);
            }
        }

    }

     public void resolvePath(PathItem pathItem){
        for(Operation operation : pathItem.readOperations()) {
            // inputs
            if (operation.getParameters() != null) {
                for (Parameter parameter : operation.getParameters()) {
                    if (parameter.getSchema() != null) {
                        Schema resolved = parameter.getSchema();
                        if (resolved != null) {
                            for (String name : schemas.keySet()) {
                                Schema schema = schemas.get(name);
                                if (resolved.equals(schema)){
                                    final String constant = (String) schema.getExtensions().get(Constants.X_SWAGGER_ROUTER_MODEL);
                                    if (constant != null) {
                                        resolved.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                    }
                                }
                            }

                            parameter.setSchema(resolved);
                        }
                    }
                    if(parameter.getContent() != null){
                        Map<String,MediaType> content = parameter.getContent();
                        for (String key: content.keySet()){
                            if (content.get(key) != null && content.get(key).getSchema() != null ){
                                Schema resolved = content.get(key).getSchema();
                                if (resolved != null) {
                                    for (String name : schemas.keySet()) {
                                        Schema schema = schemas.get(name);
                                        if (resolved.equals(schema)){
                                            final String constant = (String) schema.getExtensions().get(Constants.X_SWAGGER_ROUTER_MODEL);
                                            if (constant != null) {
                                                resolved.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                            }
                                        }
                                    }
                                    content.get(key).setSchema(resolved);
                                }
                            }
                        }
                    }
                }
            }

            if (operation.getCallbacks() != null){
                Map<String,Callback> callbacks = operation.getCallbacks();
                for (String name : callbacks.keySet()) {
                    Callback callback = callbacks.get(name);
                    if (callback != null) {
                        for(String callbackName : callback.keySet()) {
                            PathItem path = callback.get(callbackName);
                            if(path != null){
                                resolvePath(path);
                            }

                        }
                    }
                }
            }

            if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null){
                Map<String,MediaType> content = operation.getRequestBody().getContent();
                for (String key: content.keySet()){
                    if (content.get(key) != null && content.get(key).getSchema() != null ){
                        Schema resolved = content.get(key).getSchema();
                        if (resolved != null) {
                            for (String name : schemas.keySet()) {
                                Schema schema = schemas.get(name);
                                if (resolved.equals(schema)){
                                    final String constant = (String) schema.getExtensions().get(Constants.X_SWAGGER_ROUTER_MODEL);
                                    if (constant != null) {
                                        resolved.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                    }
                                }
                            }
                            content.get(key).setSchema(resolved);
                        }
                    }
                }
            }
            // responses
            if(operation.getResponses() != null) {
                for(String code : operation.getResponses().keySet()) {
                    ApiResponse response = operation.getResponses().get(code);
                    if (response.getContent() != null) {
                        Map<String, MediaType> content = response.getContent();
                        for(String mediaType: content.keySet()){
                            if(content.get(mediaType).getSchema() != null) {
                                Schema resolved = content.get(mediaType).getSchema();
                                if(resolved != null) {
                                    for (String name : schemas.keySet()) {
                                        Schema schema = schemas.get(name);
                                        if (resolved.equals(schema)){
                                            final String constant = (String) schema.getExtensions().get(Constants.X_SWAGGER_ROUTER_MODEL);
                                            if (constant != null) {
                                                resolved.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                            }
                                        }
                                    }
                                    response.getContent().get(mediaType).setSchema(resolved);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
