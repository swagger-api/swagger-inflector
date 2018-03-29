package io.swagger.inflector.utils;

import io.swagger.inflector.Constants;
import io.swagger.models.ArrayModel;
import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ResolverFully {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverFully.class);

    private Map<String, Model> schemas;
    private Map<String, Model> resolvedModels = new HashMap<>();
    private Map<String, Property> resolvedProperties = new HashMap<>();




    public void resolveFully(Swagger swagger) {
        if (swagger.getDefinitions() != null) {
            schemas = swagger.getDefinitions();
            if (schemas == null) {
                schemas = new HashMap<>();
            }
        }

        for(String name: schemas.keySet()) {
            Model model = schemas.get(name);
            if(model instanceof ModelImpl) {
                ModelImpl impl = (ModelImpl) model;
                if(!impl.getVendorExtensions().containsKey(Constants.X_SWAGGER_ROUTER_MODEL))
                    impl.setVendorExtension(Constants.X_SWAGGER_ROUTER_MODEL, name);
            }
            else if(model instanceof ComposedModel) {
                ComposedModel cm = (ComposedModel) model;
                if(!cm.getVendorExtensions().containsKey(Constants.X_SWAGGER_ROUTER_MODEL))
                    cm.setVendorExtension(Constants.X_SWAGGER_ROUTER_MODEL, name);
            }
            else if(model instanceof ArrayModel) {
                ArrayModel am = (ArrayModel) model;
                if(!am.getVendorExtensions().containsKey(Constants.X_SWAGGER_ROUTER_MODEL))
                    am.setVendorExtension(Constants.X_SWAGGER_ROUTER_MODEL, name);
            }
        }

        if(swagger.getPaths() != null) {
            for (String pathname : swagger.getPaths().keySet()) {
                Path pathItem = swagger.getPaths().get(pathname);
                resolvePath(pathItem);
            }
        }
    }

    public void resolvePath(Path path){
        for(Operation op : path.getOperations()) {
            // inputs
            for(Parameter parameter : op.getParameters()) {
                if(parameter instanceof BodyParameter) {
                    BodyParameter body = (BodyParameter) parameter;
                    Model schema = body.getSchema();
                    Model resolved = resolveModel(schema);
                    body.setSchema(resolved);
                }
            }

            // responses
            if(op.getResponses() != null) {
                for(String code : op.getResponses().keySet()) {
                    Response response = op.getResponses().get(code);
                    if (response.getResponseSchema() != null) {
                        Model resolved = resolveModel(response.getResponseSchema());
                        response.setResponseSchema(resolved);
                    }
                }
            }
        }
    }

    public Model resolveModel(Model schema) {
        if(schema instanceof RefModel) {
            String ref= ((RefModel)schema).get$ref();
            ref = ref.substring(ref.lastIndexOf("/") + 1);
            Model resolved = schemas.get(ref);
            if(resolved == null) {
                LOGGER.error("unresolved model " + ref);
                return schema;
            }
            if(this.resolvedModels.containsKey(ref)) {
                LOGGER.debug("avoiding infinite loop");
                return this.resolvedModels.get(ref);
            }
            this.resolvedModels.put(ref, schema);

            Model model = resolveModel(resolved);

            // if we make it without a resolution loop, we can update the reference
            this.resolvedModels.put(ref, model);
            return model;
        }

        if(schema instanceof ArrayModel) {
            ArrayModel arrayModel = (ArrayModel) schema;
            if(((RefProperty)(arrayModel.getItems())).get$ref() != null) {
                arrayModel.setItems(resolveProperty(arrayModel.getItems()));
            } else {
                arrayModel.setItems(arrayModel.getItems());
            }

            return arrayModel;
        }

        if (schema.getProperties() != null) {
            ModelImpl model = (ModelImpl) schema;
            Map<String, Property> updated = new LinkedHashMap<String, Property>();
            for(String propertyName : model.getProperties().keySet()) {
                Property property = model.getProperties().get(propertyName);
                Property resolved = resolveProperty(property);
                updated.put(propertyName, resolved);
            }

            for(String key : updated.keySet()) {
                Property property = updated.get(key);

                if(property instanceof ObjectProperty) {
                    ObjectProperty op = (ObjectProperty) property;
                    if(op.getProperties() != model.getProperties()) {
                        model.addProperty(key, property);
                    }
                    else {
                        LOGGER.debug("not adding recursive properties, using generic object");
                        model.addProperty(key, new ObjectProperty());
                    }
                }
            }
            return model;
        }
        LOGGER.error("no type match for " + schema);
        return schema;
    }

    private Property resolveProperty(Property property) {
        if(property instanceof RefProperty ){
            String ref= ((RefProperty)property).get$ref();
            ref = ref.substring(ref.lastIndexOf("/") + 1);
            Model resolved = schemas.get(ref);
            if(resolved == null) {
                LOGGER.error("unresolved model " + ref);
                return property;
            }
            if(this.resolvedModels.containsKey(ref)) {
                LOGGER.debug("avoiding infinite loop");
                return (Property) this.resolvedModels.get(ref);
            }
            //this.resolvedModels.put(ref, schema);
            this.resolvedProperties.put(ref, property);

            Model model = resolveModel(resolved);

            // if we make it without a resolution loop, we can update the reference
            this.resolvedModels.put(ref, model);
            return (Property) model;

        }else if (property instanceof ObjectProperty) {
            ObjectProperty obj = (ObjectProperty) property;
            if(obj.getProperties() != null) {
                Map<String, Property> updated = new LinkedHashMap<>();
                for(String propertyName : obj.getProperties().keySet()) {
                    Property innerProperty = obj.getProperties().get(propertyName);
                    // reference check
                    if(property != innerProperty) {
                        Property resolved = resolveProperty(innerProperty);
                        updated.put(propertyName, resolved);
                    }
                }
                obj.setProperties(updated);
            }
            return obj;
        }
            return property;
    }
}
