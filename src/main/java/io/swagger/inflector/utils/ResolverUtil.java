package io.swagger.inflector.utils;

import io.swagger.inflector.Constants;
import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResolverUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverUtil.class);

    private Map<String, Model> models;

    public void resolveFully(Swagger swagger) {
        models = swagger.getDefinitions();
        if(models == null) {
            models = new HashMap<String, Model>();
        }

        for(String name: models.keySet()) {
            Model model = models.get(name);
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

        for(String pathname : swagger.getPaths().keySet()) {
            Path path = swagger.getPath(pathname);
            for(Operation op : path.getOperations()) {
                // inputs
                for(Parameter parameter : op.getParameters()) {
                    if(parameter instanceof BodyParameter) {
                        BodyParameter body = (BodyParameter) parameter;
                        Model resolved = resolveFully(body.getSchema());
                        body.setSchema(resolved);
                    }
                }

                // responses
                if(op.getResponses() != null) {
                    for(String code : op.getResponses().keySet()) {
                        Response response = op.getResponses().get(code);
                        if (response.getSchema() != null) {
                            Property resolved = resolveFully(response.getSchema());
                            response.setSchema(resolved);
                        }
                    }
                }
            }
        }
    }

    public Model resolveFully(Model schema) {
        if(schema instanceof RefModel) {
            RefModel ref = (RefModel) schema;
            Model resolved = models.get(ref.getSimpleRef());
            return resolveFully(resolved);
        }
        if(schema instanceof ArrayModel) {
            ArrayModel arrayModel = (ArrayModel) schema;
            Property property = arrayModel.getItems();
            if(property instanceof RefProperty) {
                Property resolved = resolveFully(property);
                arrayModel.setItems(resolved);
            }
            return arrayModel;
        }
        if(schema instanceof ModelImpl) {
            ModelImpl model = (ModelImpl) schema;
            if(model.getProperties() != null) {
                Map<String, Property> updated = new LinkedHashMap<String, Property>();
                for(String propertyName : model.getProperties().keySet()) {
                    Property property = model.getProperties().get(propertyName);
                    Property resolved = resolveFully(property);
                    updated.put(propertyName, resolved);
                }
                model.setProperties(updated);
                return model;
            }
        }
        LOGGER.error("no type match for " + schema);
        return schema;
    }

    public Property resolveFully(Property property) {
        if(property instanceof RefProperty) {
            RefProperty ref = (RefProperty) property;
            Model model = models.get(ref.getSimpleRef());
            if(model == null) {
                LOGGER.error("unresolved model " + ref.getSimpleRef());
                return property;
            }
            else {
                return createObjectProperty(model);
            }
        }
        else if (property instanceof ObjectProperty) {
            ObjectProperty obj = (ObjectProperty) property;
            if(obj.getProperties() != null) {
                Map<String, Property> updated = new LinkedHashMap<String, Property>();
                for(String propertyName : obj.getProperties().keySet()) {
                    Property innerProperty = obj.getProperties().get(propertyName);
                    Property resolved = resolveFully(innerProperty);
                    updated.put(propertyName, resolved);
                }
                obj.setProperties(updated);
            }
            return obj;
        }
        else if (property instanceof ArrayProperty) {
            ArrayProperty array = (ArrayProperty) property;
            if(array.getItems() != null) {
                Property resolved = resolveFully(array.getItems());
                array.setItems(resolved);
            }
            return array;
        }
        return property;
    }

    public Property createObjectProperty(Model model) {
        if(model instanceof ModelImpl) {
            ModelImpl m = (ModelImpl) resolveFully(model);
            ObjectProperty property = new ObjectProperty();
            property.setProperties(m.getProperties());
            property.setName(m.getName());
            property.setFormat(m.getFormat());
            property.setDefault(m.getDefaultValue());
            property.setDescription(m.getDescription());
            property.setXml(m.getXml());

            if(m.getExample() != null) {
                property.setExample(m.getExample().toString());
            }

            return property;
        }
        if(model instanceof ArrayModel) {
            ArrayModel m = (ArrayModel) model;
            ArrayProperty property = new ArrayProperty();
            Property inner = m.getItems();
            Property resolved = resolveFully(inner);
            property.setItems(resolved);
            property.setDescription(m.getDescription());

            return property;
        }
        if(model instanceof RefModel) {
            RefModel ref = (RefModel) model;
            Model inner = models.get(ref.getSimpleRef());
            return createObjectProperty(inner);
        }
        LOGGER.error("can't resolve " + model);
        return null;
    }
}
