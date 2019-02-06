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
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.UntypedProperty;
import io.swagger.models.utils.PropertyModelConverter;
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

    private Map<String, Model> schemas;
    private Map<String, Model> resolvedModels = new HashMap<>();
    private Map<String, Integer> timesResolvedModels = new HashMap<>();
    private Map<String, Property> resolvedProperties = new HashMap<>();
    private Map<String, Integer> timesResolvedProperties = new HashMap<>();
    int counter = 0;

    public Map<String, Model> getResolvedModels() {
        return resolvedModels;
    }

    public void resolveFully(Swagger swagger) {
        if (swagger.getDefinitions() != null) {
            schemas = swagger.getDefinitions();
        }
        if (schemas == null) {
            schemas = new HashMap<>();
        }

        for (String name : schemas.keySet()) {
            Model model = schemas.get(name);
            if (model instanceof ModelImpl) {
                ModelImpl impl = (ModelImpl) model;
                if (!impl.getVendorExtensions().containsKey(Constants.X_SWAGGER_ROUTER_MODEL))
                    impl.setVendorExtension(Constants.X_SWAGGER_ROUTER_MODEL, name);
            } else if (model instanceof ComposedModel) {
                ComposedModel cm = (ComposedModel) model;
                if (!cm.getVendorExtensions().containsKey(Constants.X_SWAGGER_ROUTER_MODEL))
                    cm.setVendorExtension(Constants.X_SWAGGER_ROUTER_MODEL, name);
            } else if (model instanceof ArrayModel) {
                ArrayModel am = (ArrayModel) model;
                if (!am.getVendorExtensions().containsKey(Constants.X_SWAGGER_ROUTER_MODEL))
                    am.setVendorExtension(Constants.X_SWAGGER_ROUTER_MODEL, name);
            }
        }

        if (swagger.getPaths() != null) {
            for (String pathname : swagger.getPaths().keySet()) {
                Path pathItem = swagger.getPaths().get(pathname);
                resolvePath(pathItem);
            }
        }
    }

    public void resolvePath(Path path) {
        for (Operation op : path.getOperations()) {
            // inputs
            for (Parameter parameter : op.getParameters()) {
                if (parameter instanceof BodyParameter) {
                    BodyParameter body = (BodyParameter) parameter;
                    Model schema = body.getSchema();
                    Model resolved = resolveModel(schema);
                    body.setSchema(resolved);
                }
            }

            // responses
            if (op.getResponses() != null) {
                for (String code : op.getResponses().keySet()) {
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
        if (schema instanceof RefModel) {
            String ref = ((RefModel) schema).getSimpleRef();
            Model resolved = schemas.get(ref);
            if (resolved == null) {
                LOGGER.error("unresolved model " + ref);
                return schema;
            }
            if (this.resolvedModels.containsKey(ref)) {
                LOGGER.debug("avoiding infinite loop");
                return this.resolvedModels.get(ref);
            }
            this.resolvedModels.put(ref, schema);

            Model model = resolveModel(resolved);

            // if we make it without a resolution loop, we can update the reference
            this.resolvedModels.put(ref, model);
            this.timesResolvedModels.put(ref,counter);
            counter++;
            return model;
        }
        if (schema instanceof ArrayModel) {
            ArrayModel arrayModel = (ArrayModel) schema;
            Property property = arrayModel.getItems();
            if (property instanceof RefProperty) {
                Property resolved = resolveProperty(property);
                arrayModel.setItems(resolved);
            }
            return arrayModel;
        }
        if (schema instanceof ModelImpl) {
            ModelImpl model = (ModelImpl) schema;
            if (model.getProperties() != null) {
                Map<String, Property> updated = new LinkedHashMap<String, Property>();
                for (String propertyName : model.getProperties().keySet()) {
                    Property property = model.getProperties().get(propertyName);
                    if(resolvedProperties.get(propertyName) == null || resolvedProperties.get(propertyName) != property) {
                        LOGGER.debug("avoiding infinite loop");
                        Property resolved = resolveProperty(property);
                        updated.put(propertyName, resolved);
                        resolvedProperties.put(propertyName, resolved);
                    }else {
                        updated.put(propertyName, resolvedProperties.get(propertyName));
                    }
                }

                for (String key : updated.keySet()) {
                    Property property = updated.get(key);

                    if (property instanceof ObjectProperty) {
                        ObjectProperty op = (ObjectProperty) property;
                        if (op.getProperties() != model.getProperties()) {
                            model.addProperty(key, property);
                        } else {
                            LOGGER.debug("not adding recursive properties, using generic object");
                            model.addProperty(key, new ObjectProperty());
                        }
                    }
                }
                return model;
            }
        }
        if (schema instanceof ComposedModel) {
            ComposedModel composedSchema = (ComposedModel) schema;
            ModelImpl model = new ModelImpl();
            Set<String> requiredProperties = new HashSet<>();
            if (composedSchema.getAllOf() != null) {
                for (Model innerModel : composedSchema.getAllOf()) {

                Model resolved = resolveModel(innerModel);
                Map<String, Property> properties = resolved.getProperties();
                if (resolved.getProperties() != null) {
                    for (String key : properties.keySet()) {
                        Property prop = resolved.getProperties().get(key);
                        if(resolvedProperties.get(key) == null || resolvedProperties.get(key) != prop) {
                            LOGGER.debug("avoiding infinite loop");
                            Property resolvedProp = resolveProperty(prop);
                            if (prop.getRequired()) {
                                requiredProperties.add(key);
                            }
                            model.addProperty(key,resolvedProp );
                            resolvedProperties.put(key, resolvedProp);
                        }else {
                            model.addProperty(key,resolvedProperties.get(key));
                        }
                    }

                    }
                    if (requiredProperties.size() > 0) {
                        model.setRequired(new ArrayList<>(requiredProperties));
                    }
                    if (composedSchema.getVendorExtensions() != null) {
                        Map<String, Object> extensions = composedSchema.getVendorExtensions();
                        for (String key : extensions.keySet()) {
                            model.setVendorExtension(key, composedSchema.getVendorExtensions().get(key));
                        }
                    }
                }
            }
            return model;
        }
        LOGGER.error("no type match for " + schema);
        return schema;
    }

    private Property resolveProperty(Property property) {
        boolean alreadyResolved  = false;
        if (property instanceof RefProperty) {
            String ref = ((RefProperty) property).getSimpleRef();
            Model resolved = schemas.get(ref);
            if (resolved == null) {
                LOGGER.error("unresolved model " + ref);
                return property;
            }
            if (this.resolvedModels.containsKey(ref) || this.resolvedProperties.containsKey(ref)) {
                alreadyResolved = true;
                LOGGER.debug("avoiding infinite loop");
                Model modelResolved = this.resolvedModels.get(ref);
                Property propertyResolved = this.resolvedProperties.get(ref);

                if (modelResolved != null) {
                    PropertyModelConverter converter = new PropertyModelConverter();
                    Property convertedProperty = converter.modelToProperty(modelResolved);
                    if (convertedProperty instanceof UntypedProperty && modelResolved instanceof ModelImpl) {
                        Property property1 = createObjectProperty(modelResolved);
                        this.resolvedProperties.put(ref, property1);
                        return property1;
                    } else {
                        return convertedProperty;
                    }
                } else if (propertyResolved != null) {
                    return propertyResolved;
                }

            }

            this.resolvedProperties.put(ref, property);
            this.timesResolvedProperties.put(ref,counter++);
            if (alreadyResolved) {
                counter++;
            }
            Model model = resolveModel(resolved);

            // if we make it without a resolution loop, we can update the reference
            this.resolvedModels.put(ref, model);
            PropertyModelConverter converter = new PropertyModelConverter();
            Property prop = converter.modelToProperty(model);
            if (prop instanceof UntypedProperty && model instanceof ModelImpl) {
                Property property1 = createObjectProperty(model);
                this.resolvedProperties.put(ref, property1);
                return property1;
            } else {
                return prop;
            }

        } else if (property instanceof ObjectProperty) {
            ObjectProperty obj = (ObjectProperty) property;
            if (obj.getProperties() != null) {
                Map<String, Property> updated = new LinkedHashMap<>();
                for (String propertyName : obj.getProperties().keySet()) {
                    Property innerProperty = obj.getProperties().get(propertyName);
                    // reference check
                    if (property != innerProperty) {
                        if(resolvedProperties.get(propertyName) == null || resolvedProperties.get(propertyName) != innerProperty) {
                            LOGGER.debug("avoiding infinite loop");
                            Property resolved = resolveProperty(innerProperty);
                            updated.put(propertyName, resolved);
                            resolvedProperties.put(propertyName, resolved);
                        }else {
                            updated.put(propertyName, resolvedProperties.get(propertyName));
                        }
                    }
                }
                obj.setProperties(updated);
            }
            return obj;
        } else if (property instanceof ArrayProperty) {
            ArrayProperty array = (ArrayProperty) property;
            if(array.getItems() instanceof RefProperty ) {
                array.setItems(resolveProperty(array.getItems()));
            } else {
                array.setItems(array.getItems());
            }

            return array;
        }
        return property;
    }

    public Property createObjectProperty(Model model) {
        if (model instanceof ModelImpl) {
            ModelImpl m = (ModelImpl) model;
            ObjectProperty property = new ObjectProperty();
            property.setProperties(m.getProperties());
            property.setName(m.getName());
            property.setFormat(m.getFormat());
            if (m.getDefaultValue() != null) {
                property.setDefault(m.getDefaultValue().toString());
            }
            property.setDescription(m.getDescription());
            property.setXml(m.getXml());

            if (m.getExample() != null) {
                property.setExample(m.getExample().toString());
            }
            final String name = (String) m.getVendorExtensions()
                    .get(Constants.X_SWAGGER_ROUTER_MODEL);
            if (name != null) {
                property.setVendorExtension(Constants.X_SWAGGER_ROUTER_MODEL, name);
            }

            return property;
        }
        if (model instanceof ArrayModel) {
            ArrayModel m = (ArrayModel) model;
            ArrayProperty property = new ArrayProperty();
            Property inner = m.getItems();
            Property resolved = resolveProperty(inner);
            property.setItems(resolved);
            property.setDescription(m.getDescription());

            return property;
        }
        if (model instanceof RefModel) {
            RefModel ref = (RefModel) model;
            Model inner = schemas.get(ref.getSimpleRef());
            return createObjectProperty(inner);
        }
        if (model instanceof ComposedModel) {
            ObjectProperty op = new ObjectProperty();

            ComposedModel cm = (ComposedModel) model;
            Set<String> requiredProperties = new HashSet<>();
            for (Model item : cm.getAllOf()) {
                Property itemProperty = createObjectProperty(item);
                if (itemProperty instanceof ObjectProperty) {
                    ObjectProperty itemPropertyObject = (ObjectProperty) itemProperty;
                    if (itemPropertyObject.getProperties() != null) {
                        for (String key : itemPropertyObject.getProperties().keySet()) {
                            op.property(key, itemPropertyObject.getProperties().get(key));
                        }
                    }
                    if (itemPropertyObject.getRequiredProperties() != null) {
                        for (String req : itemPropertyObject.getRequiredProperties()) {
                            requiredProperties.add(req);
                        }
                    }
                }
            }
            if (requiredProperties.size() > 0) {
                op.setRequiredProperties(new ArrayList(requiredProperties));
            }
            if (cm.getVendorExtensions() != null) {
                for (String key : cm.getVendorExtensions().keySet()) {
                    op.vendorExtension(key, cm.getVendorExtensions().get(key));
                }
            }
            return op;
        }
        LOGGER.error("can't resolve " + model);
        return null;
    }
}