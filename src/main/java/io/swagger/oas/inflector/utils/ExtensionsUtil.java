package io.swagger.oas.inflector.utils;

import io.swagger.oas.inflector.Constants;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.callbacks.Callback;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.Map;

public class ExtensionsUtil {

    private Map<String, Schema> schemas;

    public  void removeExtensions(OpenAPI openAPI) {

        if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) {
            schemas = new HashMap<>();
        } else {
            schemas = openAPI.getComponents().getSchemas();
            for (String name : schemas.keySet()) {
                Schema schema = schemas.get(name);
                if (schema.getExtensions() != null) {
                    if (schema.getExtensions().containsKey(Constants.X_SWAGGER_ROUTER_MODEL)) {
                        Map<String,Schema> extensions = schema.getExtensions();
                        Object value = extensions.get(Constants.X_SWAGGER_ROUTER_MODEL);
                        extensions.remove(Constants.X_SWAGGER_ROUTER_MODEL,value);
                    }
                }
            }
        }
        if (openAPI.getPaths() != null) {
            for (String pathname : openAPI.getPaths().keySet()) {
                PathItem pathItem = openAPI.getPaths().get(pathname);
                resolvePath(pathItem,false);
            }
        }

    }

    public void addExtensions(OpenAPI openAPI) {
        if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) {
            schemas = new HashMap<>();
        } else {
            schemas = openAPI.getComponents().getSchemas();
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
                resolvePath(pathItem, true);
            }
        }

    }

     public void resolvePath(PathItem pathItem, boolean addExtensions){
        for(Operation operation : pathItem.readOperations()) {
            // Removing extension if called by removeExtensions.
            if (!addExtensions) {
                if(operation.getExtensions() != null) {
                    final String controllerExtension = (String) operation.getExtensions().get(Constants.X_SWAGGER_ROUTER_CONTROLLER);
                    if (controllerExtension != null) {
                        operation.getExtensions().remove(Constants.X_SWAGGER_ROUTER_CONTROLLER, controllerExtension);
                    }
                }
            }
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
                                        if (addExtensions) {
                                            resolved.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                        }else {
                                            resolved.getExtensions().remove(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                        }
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
                                                if (addExtensions) {
                                                    resolved.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                                }else {
                                                    resolved.getExtensions().remove(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                                }
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
                                resolvePath(path,addExtensions);
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
                                    if (schema.getExtensions() != null){
                                        final String constant = (String) schema.getExtensions().get(Constants.X_SWAGGER_ROUTER_MODEL);
                                        if (constant != null) {
                                            if (addExtensions) {
                                                resolved.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                            } else {
                                                resolved.getExtensions().remove(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                            }
                                        }
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
                                            if (schema.getExtensions() != null) {
                                                final String constant = (String) schema.getExtensions().get(Constants.X_SWAGGER_ROUTER_MODEL);
                                                if (constant != null) {
                                                    if (addExtensions) {
                                                        resolved.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                                    } else {
                                                        resolved.getExtensions().remove(Constants.X_SWAGGER_ROUTER_MODEL, constant);
                                                    }
                                                }
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
