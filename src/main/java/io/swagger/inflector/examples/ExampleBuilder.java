/*
 *  Copyright 2016 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.swagger.inflector.examples;

import io.swagger.inflector.examples.models.ArrayExample;
import io.swagger.inflector.examples.models.BooleanExample;
import io.swagger.inflector.examples.models.DecimalExample;
import io.swagger.inflector.examples.models.DoubleExample;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.examples.models.FloatExample;
import io.swagger.inflector.examples.models.IntegerExample;
import io.swagger.inflector.examples.models.LongExample;
import io.swagger.inflector.examples.models.ObjectExample;
import io.swagger.inflector.examples.models.StringExample;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Xml;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.EmailProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.UUIDProperty;
import io.swagger.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExampleBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleBuilder.class);

    public static Example fromProperty(Property property, Map<String, Model> definitions) {
        return fromProperty(property, definitions, new HashSet<String>());
    }

    public static Example fromProperty(Property property, Map<String, Model> definitions, Set<String> processedModels) {
        if (property == null) {
            return null;
        }

        String name = null;
        String namespace = null;
        String prefix = null;
        Boolean attribute = false;
        Boolean wrapped = false;

        if (property.getXml() != null) {
            Xml xml = property.getXml();
            name = xml.getName();
            namespace = xml.getNamespace();
            prefix = xml.getPrefix();
            attribute = xml.getAttribute();
            wrapped = xml.getWrapped() != null ? xml.getWrapped() : false;
        }

        Example output = null;

        Object example = property.getExample();
        if (property instanceof RefProperty) {
            RefProperty ref = (RefProperty) property;
            if(processedModels.contains(ref.getSimpleRef())) {
                return null;
            }
            processedModels.add(ref.getSimpleRef());
            Model model = definitions.get(ref.getSimpleRef());
            if (model != null) {
                if (model.getExample() != null) {
                    try {
                        String str = model.getExample().toString();
                        output = Json.mapper().readValue(str, ObjectExample.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        } else if (property instanceof EmailProperty) {
            if (example != null) {
                return new StringExample(example.toString());
            }
            output = new StringExample("apiteam@swagger.io");
        } else if (property instanceof UUIDProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            output = new StringExample("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        } else if (property instanceof StringProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            } else {
                output = new StringExample("string");
            }
        } else if (property instanceof IntegerProperty) {
            if (example != null) {
                output = new IntegerExample(Integer.parseInt(example.toString()));
            } else {
                output = new IntegerExample(0);
            }
        } else if (property instanceof LongProperty) {
            if (example != null) {
                output = new LongExample(Long.parseLong(example.toString()));
            }
            output = new LongExample(0);
        } else if (property instanceof FloatProperty) {
            if (example != null) {
                output = new FloatExample(Float.parseFloat(example.toString()));
            }
            output = new FloatExample(1.1f);
        } else if (property instanceof DoubleProperty) {
            if (example != null) {
                output = new DoubleExample(Double.parseDouble(example.toString()));
            }
            output = new DoubleExample(1.23);
        } else if (property instanceof BooleanProperty) {
            if (example != null) {
                output = new BooleanExample(Boolean.valueOf(Boolean.parseBoolean(example.toString())));
            }
            output = new BooleanExample(Boolean.valueOf(true));
        } else if (property instanceof DateProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            output = new StringExample("2015-07-20");
        } else if (property instanceof DateTimeProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            output = new StringExample("2015-07-20T15:49:04-07:00");
        } else if (property instanceof DecimalProperty) {
            if (example != null) {
                output = new DecimalExample(new BigDecimal(example.toString()));
            }
            output = new DecimalExample(new BigDecimal(1.5));
        } else if (property instanceof ObjectProperty) {
            if (example != null) {
                try {
                    output = Json.mapper().readValue(example.toString(), ObjectExample.class);
                } catch (IOException e) {
                    LOGGER.error("unable to convert `" + example + "` to JsonNode");
                    output = new ObjectExample();
                }
            }
            else {
                ObjectExample outputExample = new ObjectExample();
                outputExample.setName( property.getName() );
                ObjectProperty op = (ObjectProperty) property;
                if(op.getProperties() != null) {
                    for(String propertyname : op.getProperties().keySet()) {
                        Property inner = op.getProperties().get(propertyname);
                        Example innerExample = fromProperty(inner, definitions);
                        outputExample.put(propertyname, innerExample);
                    }
                }
                output = outputExample;
            }
        } else if (property instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) property;
            Property inner = ap.getItems();
            if (inner != null) {
                Object innerExample = fromProperty(inner, definitions, processedModels);
                if (innerExample != null) {
                    if (innerExample instanceof Example) {
                        ArrayExample an = new ArrayExample();
                        an.add((Example) innerExample);
                        an.setName(property.getName());
                        output = an;
                    }
                }
            }
        } else if (property instanceof MapProperty) {
            MapProperty mp = (MapProperty) property;
            Property inner = mp.getAdditionalProperties();
            if (inner != null) {
                Object innerExample = fromProperty(inner, definitions, processedModels);
                if (innerExample != null) {
                    ObjectExample on = new ObjectExample();

                    if (innerExample instanceof Example) {
                        StringExample key = new StringExample("key");
                        key.setName("key");
                        on.put("key", key);
                        Example in = (Example) innerExample;
                        if (in.getName() == null) {
                            in.setName("value");
                        }
                        on.put("value", (Example) in);
                        output = on;
                    } else {
                        ObjectExample outputMap = new ObjectExample();
                        outputMap.put("key", new ObjectExample());
                        output = outputMap;
                    }
                }
            }
        }

        // TODO: File
        if (property instanceof RefProperty && output == null) {
            RefProperty ref = (RefProperty) property;
            Model model = definitions.get(ref.getSimpleRef());
            if (model != null) {
                if (model instanceof ModelImpl) {
                    ModelImpl i = (ModelImpl) model;
                    if (i.getXml() != null) {
                        Xml xml = i.getXml();
                        name = xml.getName();
                        attribute = xml.getAttribute();
                        namespace = xml.getNamespace();
                        prefix = xml.getPrefix();
                        wrapped = xml.getWrapped();
                    }
                }
                if (model.getExample() != null) {
                    try {
                        Example n = Json.mapper().readValue(model.getExample().toString(), Example.class);
                        output = n;
                    } catch (IOException e) {
                        LOGGER.error("unable to convert value", e);
                    }
                } else {
                    ObjectExample values = new ObjectExample();

                    Map<String, Property> properties = model.getProperties();
                    for (String key : properties.keySet()) {
                        Property innerProp = properties.get(key);
                        Example p = (Example) fromProperty(innerProp, definitions, processedModels);
                        if (p != null) {
                            if (p.getName() == null) {
                                p.setName(key);
                            }
                            values.put(key, p);
                            processedModels.add(key);
                        }
                    }
                    output = values;
                }
            }
            if (output != null) {
                output.setName(ref.getSimpleRef());
            }
        }
        if (output != null) {
            if (attribute != null) {
                output.setAttribute(attribute);
            }
            if (wrapped != null && wrapped) {
                if (name != null) {
                    output.setWrappedName(name);
                }
            } else if (name != null) {
                output.setName(name);
            }
            output.setNamespace(namespace);
            output.setPrefix(prefix);
            output.setWrapped(wrapped);
        }
        return output;
    }
}
