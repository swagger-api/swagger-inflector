package io.swagger.oas.inflector.utils;

import io.swagger.oas.inflector.Constants;

import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.callbacks.Callback;
import io.swagger.oas.models.media.AllOfSchema;
import io.swagger.oas.models.media.ArraySchema;
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

public class ResolverUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverUtil.class);

    private Map<String, Schema> schemas;
    private Map<String, Schema> resolvedModels = new HashMap<>();
    private Map<String, Schema> resolvedProperties = new HashMap<>();



    public void resolveFully(OpenAPI openAPI) {

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
        if(openAPI.getPaths() != null) {
            for (String pathname : openAPI.getPaths().keySet()) {
                PathItem pathItem = openAPI.getPaths().get(pathname);
                resolvePath(pathItem);
            }
        }
    }

     public void resolvePath(PathItem pathItem){
        for(Operation op : pathItem.readOperations()) {
            // inputs
            if (op.getParameters() != null) {
                for (Parameter parameter : op.getParameters()) {
                    if (parameter.getSchema() != null) {
                        Schema resolved = resolveSchema(parameter.getSchema());
                        if (resolved != null) {
                            parameter.setSchema(resolved);
                        }
                    }
                    if(parameter.getContent() != null){
                        Map<String,MediaType> content = parameter.getContent();
                        for (String key: content.keySet()){
                            if (content.get(key) != null && content.get(key).getSchema() != null ){
                                Schema resolvedSchema = resolveSchema(content.get(key).getSchema());
                                if (resolvedSchema != null) {
                                    content.get(key).setSchema(resolvedSchema);
                                }
                            }
                        }
                    }
                }
            }

            if (op.getCallbacks() != null){
                Map<String,Callback> callbacks = op.getCallbacks();
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

            if (op.getRequestBody() != null && op.getRequestBody().getContent() != null){
                Map<String,MediaType> content = op.getRequestBody().getContent();
                for (String key: content.keySet()){
                    if (content.get(key) != null && content.get(key).getSchema() != null ){
                        Schema resolved = resolveSchema(content.get(key).getSchema());
                        if (resolved != null) {
                            content.get(key).setSchema(resolved);
                        }
                    }
                }
            }
            // responses
            if(op.getResponses() != null) {
                for(String code : op.getResponses().keySet()) {
                    ApiResponse response = op.getResponses().get(code);
                    if (response.getContent() != null) {
                        Map<String, MediaType> content = response.getContent();
                        for(String mediaType: content.keySet()){
                            if(content.get(mediaType).getSchema() != null) {
                                Schema resolved = resolveSchema(content.get(mediaType).getSchema());
                                response.getContent().get(mediaType).setSchema(resolved);
                            }
                        }
                    }
                }
            }
        }
    }


    public Schema resolveSchema(Schema schema) {
        if(schema.get$ref() != null) {
            String ref= schema.get$ref();
            ref = ref.substring(ref.lastIndexOf("/") + 1);
            Schema resolved = schemas.get(ref);
            if(resolved == null) {
                LOGGER.error("unresolved model " + ref);
                return schema;
            }
            if(this.resolvedModels.containsKey(ref)) {
                LOGGER.debug("avoiding infinite loop");
                return this.resolvedModels.get(ref);
            }
            this.resolvedModels.put(ref, schema);

            Schema model = resolveSchema(resolved);

            // if we make it without a resolution loop, we can update the reference
            this.resolvedModels.put(ref, model);
            return model;
        }
        if(schema instanceof ArraySchema) {
            ArraySchema arrayModel = (ArraySchema) schema;
            Schema items = arrayModel.getItems();
            if(items.get$ref() != null) {
                Schema resolved = resolveSchema(items);
                arrayModel.setItems(resolved);
            }
            return arrayModel;
        }

        if(schema.getProperties() != null) {
            Schema model = schema;
            Map<String, Schema> updated = new LinkedHashMap<>();
            Map<String, Schema> properties = model.getProperties();
            for(String propertyName : properties.keySet()) {
                Schema property = (Schema) model.getProperties().get(propertyName);
                Schema resolved = resolveProperty(property);
                updated.put(propertyName, resolved);
            }

            for(String key : updated.keySet()) {
                Schema property = updated.get(key);

                if(property.getProperties() != model.getProperties()) {
                    model.addProperties(key, property);
                }
                else {
                    LOGGER.debug("not adding recursive properties, using generic object");
                    schema.addProperties(key, new Schema());
                }

            }
            return model;
        }

        if(schema instanceof AllOfSchema) {
            AllOfSchema allOfSchema = (AllOfSchema) schema;
            Schema model = new Schema();
            Set<String> requiredProperties = new HashSet<>();
            for(Schema innerModel : allOfSchema.getAllOf()) {
                Schema resolved = resolveProperty(innerModel);
                if(resolved instanceof Schema) {
                    Map<String, Schema> properties = resolved.getProperties();
                    if(resolved.getProperties() != null) {
                        int count = 0;
                        for(String key : properties.keySet()) {
                            Schema prop = (Schema)resolved.getProperties().get(key);
                            if(prop.getRequired() != null) {
                                if(prop.getRequired().get(count) != null) {
                                    requiredProperties.add(key);
                                }
                            }
                            count ++;
                            model.addProperties(key, resolveSchema(prop));
                        }
                    }
                }
            }
            if(requiredProperties.size() > 0) {
                model.setRequired(new ArrayList<>(requiredProperties));
            }
            if(allOfSchema.getExtensions() != null) {
                Map<String, Object> extensions = allOfSchema.getExtensions();
                for(String key : extensions.keySet()) {
                    model.addExtension(key, allOfSchema.getExtensions().get(key));
                }
            }
            return model;
        }
        LOGGER.error("no type match for " + schema);
        return schema;
    }

    public Schema resolveProperty(Schema property) {
        if(property.get$ref() != null) {
            String ref= property.get$ref();
            ref = ref.substring(ref.lastIndexOf("/") + 1);
            if(this.resolvedProperties.containsKey(ref)){
                Schema resolved = this.resolvedProperties.get(ref);
                // don't return full recursion, check object address
                if(resolved == property) {
                    LOGGER.debug("avoiding infinite loop, using generic object property");
                    return new Schema();
                }
                return this.resolvedProperties.get(ref);
            }

            this.resolvedProperties.put(ref, property);
            Schema model = schemas.get(ref);
            if(model == null) {
                LOGGER.error("unresolved model " + ref);
                return property;
            }
            else {
                Schema output = createObjectProperty(model);
                this.resolvedProperties.put(ref, output);
                return output;
            }
        }
        else if(property.getProperties() != null) {
            Map<String, Schema> updated = new LinkedHashMap<>();
            Map<String, Schema> properties = property.getProperties();
            for(String propertyName : properties.keySet()) {
                Schema innerProperty = (Schema)property.getProperties().get(propertyName);
                // reference check
                if(property != innerProperty) {
                    Schema resolved = resolveProperty(innerProperty);
                    updated.put(propertyName, resolved);
                }
            }
            property.setProperties(updated);
            return property;
        }
        else if (property instanceof ArraySchema) {
            ArraySchema array = (ArraySchema) property;
            if(array.getItems() != null) {
                Schema resolved = resolveProperty(array.getItems());
                array.setItems(resolved);
            }
            return array;
        }
        return property;
    }

    public Schema createObjectProperty(Schema model) {

         if (model.getProperties() != null) {
             Schema property = new Schema();
             property.setProperties(model.getProperties());
             //property.setName(model.getName());
             property.setFormat(model.getFormat());
             if (model.getDefault() != null) {
                 property.setDefault(model.getDefault().toString());
             }
             property.setDescription(model.getDescription());
             property.setXml(model.getXml());

             if (model.getExample() != null) {
                 property.setExample(model.getExample().toString());
             }
             final String name = (String) model.getExtensions()
                     .get(Constants.X_SWAGGER_ROUTER_MODEL);
             if (name != null) {
                 property.addExtension(Constants.X_SWAGGER_ROUTER_MODEL, name);
             }

             return property;
         }


        if(model instanceof ArraySchema) {
            ArraySchema m = (ArraySchema) model;
            ArraySchema property = new ArraySchema();
            Schema inner = m.getItems();
            Schema resolved = resolveProperty(inner);
            property.setItems(resolved);
            property.setDescription(m.getDescription());

            return property;
        }
        if(model.get$ref() != null) {
            String ref= model.get$ref();
            ref = ref.substring(ref.lastIndexOf("/") + 1);
            Schema inner = schemas.get(ref);
            return createObjectProperty(inner);
        }
        if(model instanceof AllOfSchema) {
           Schema op = new Schema();

            AllOfSchema allOfSchema = (AllOfSchema) model;
            Set<String> requiredProperties = new HashSet<>();
            for(Schema item : allOfSchema.getAllOf()) {
                Schema itemProperty = createObjectProperty(item);
                if(itemProperty.getProperties() != null) {
                    Map<String,Schema> properties = itemProperty.getProperties();
                    for (String key : properties.keySet()) {
                        op.addProperties(key, (Schema) itemProperty.getProperties().get(key));
                    }
                }
                if(itemProperty.getRequired() != null) {
                    for(Object req : itemProperty.getRequired()) {
                        requiredProperties.add(req.toString());
                    }
                }

            }
            if(requiredProperties.size() > 0) {
                op.setRequired(new ArrayList(requiredProperties));
            }
            if(allOfSchema.getExtensions() != null) {
                Map<String, Object> extensions = allOfSchema.getExtensions();
                for(String key : extensions.keySet()) {
                    op.addExtension(key, allOfSchema.getExtensions().get(key));
                }
            }
            return op;
        }
        LOGGER.error("can't resolve " + model);
        return null;
    }
}
