/*
 *  Copyright 2017 SmartBear Software
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

package io.swagger.oas.inflector.examples;

import io.swagger.oas.inflector.examples.models.ArrayExample;
import io.swagger.oas.inflector.examples.models.BooleanExample;
import io.swagger.oas.inflector.examples.models.DecimalExample;
import io.swagger.oas.inflector.examples.models.DoubleExample;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.examples.models.FloatExample;
import io.swagger.oas.inflector.examples.models.IntegerExample;
import io.swagger.oas.inflector.examples.models.LongExample;
import io.swagger.oas.inflector.examples.models.ObjectExample;
import io.swagger.oas.inflector.examples.models.StringExample;


import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.BooleanSchema;
import io.swagger.oas.models.media.ComposedSchema;
import io.swagger.oas.models.media.DateSchema;
import io.swagger.oas.models.media.DateTimeSchema;
import io.swagger.oas.models.media.EmailSchema;
import io.swagger.oas.models.media.IntegerSchema;
import io.swagger.oas.models.media.NumberSchema;
import io.swagger.oas.models.media.ObjectSchema;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.media.StringSchema;
import io.swagger.oas.models.media.UUIDSchema;
import io.swagger.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.oas.models.media.XML;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ExampleBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleBuilder.class);

    public static final String SAMPLE_EMAIL_PROPERTY_VALUE = "apiteam@swagger.io";
    public static final String SAMPLE_UUID_PROPERTY_VALUE = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
    public static final String SAMPLE_STRING_PROPERTY_VALUE = "string";
    public static final int SAMPLE_INT_PROPERTY_VALUE = 0;
    public static final int SAMPLE_LONG_PROPERTY_VALUE = 0;
    public static final int SAMPLE_BASE_INTEGER_PROPERTY_VALUE = 0;
    public static final float SAMPLE_FLOAT_PROPERTY_VALUE = 1.1f;
    public static final double SAMPLE_DOUBLE_PROPERTY_VALUE = 1.1f;
    public static final boolean SAMPLE_BOOLEAN_PROPERTY_VALUE = true;
    public static final String SAMPLE_DATE_PROPERTY_VALUE = "2015-07-20";
    public static final String SAMPLE_DATETIME_PROPERTY_VALUE = "2015-07-20T15:49:04-07:00";
    public static final double SAMPLE_DECIMAL_PROPERTY_VALUE = 1.5;

    public static Example fromProperty(Schema property, Map<String, Schema> definitions) {
        return fromProperty(property, definitions, new HashSet<String>());
    }

    public static Example fromProperty(Schema property, Map<String, Schema> definitions, Set<String> processedModels) {
        if (property == null) {
            return null;
        }

        String name = null;
        String namespace = null;
        String prefix = null;
        Boolean attribute = false;
        Boolean wrapped = false;

        if (property.getXml() != null) {
            XML xml = property.getXml();
            name = xml.getName();
            namespace = xml.getNamespace();
            prefix = xml.getPrefix();
            attribute = xml.getAttribute();
            wrapped = xml.getWrapped() != null ? xml.getWrapped() : false;
        }

        Example output = null;

        Object example = property.getExample();
        if (property.get$ref() != null) {
            String ref = property.get$ref();
            ref = ref.substring(ref.lastIndexOf("/") + 1);
            if(processedModels.contains(ref)) {
                // return some sort of example
                return alreadyProcessedRefExample(ref, definitions);
            }
            processedModels.add(ref);
            if( definitions != null ) {
                Schema model = definitions.get(ref);
                if (model != null) {
                    output = fromModel(ref, model, definitions, processedModels);
                }
            }
        } else if (property instanceof EmailSchema) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {
                String defaultValue = ((EmailSchema)property).getDefault();

                if( defaultValue == null ){
                    List<String> enums = ((EmailSchema) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new StringExample( defaultValue == null ? SAMPLE_EMAIL_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof UUIDSchema) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {
                UUID defaultValue = ((UUIDSchema)property).getDefault();


                if( defaultValue == null ){
                    List<UUID> enums = ((UUIDSchema) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new StringExample( defaultValue.toString() == null ? SAMPLE_UUID_PROPERTY_VALUE : defaultValue.toString() );
            }
        } else if (property instanceof StringSchema) {
            if (example != null) {
                output = new StringExample(example.toString());
            } else {
                String defaultValue = ((StringSchema)property).getDefault();

                if( defaultValue == null ){
                    List<String> enums = ((StringSchema) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new StringExample( defaultValue == null ? SAMPLE_STRING_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof IntegerSchema) {
            if (example != null) {
                try {
                    if (property.getFormat() != null) {
                        if (property.getFormat().equals("int32")) {
                            output = new IntegerExample(Integer.parseInt(example.toString()));
                        } else if (property.getFormat().equals("int64")) {
                            output = new LongExample(Long.parseLong(example.toString()));
                        }
                    }else{
                        output = new IntegerExample(Integer.parseInt(example.toString()));
                    }
                } catch (NumberFormatException e) {
                }
            }

            if (output == null) {
                Integer defaultValue = ((IntegerSchema) property).getDefault();

                if (defaultValue == null) {
                    List<Integer> enums = ((IntegerSchema) property).getEnum();
                    if (enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }
                if (property.getFormat() != null) {
                    if (property.getFormat().equals("int32")) {
                        output = new IntegerExample(defaultValue == null ? SAMPLE_INT_PROPERTY_VALUE : defaultValue);
                    } else if (property.getFormat().equals("int64")) {
                        output = new LongExample(defaultValue == null ? SAMPLE_LONG_PROPERTY_VALUE : defaultValue.longValue());
                    }
                }else {
                    output = new IntegerExample(SAMPLE_BASE_INTEGER_PROPERTY_VALUE);
                }
            }
        } else if (property instanceof NumberSchema) {

                    if (example != null) {
                        try {
                            if (property.getFormat() != null) {
                                if (property.getFormat().equals("double")) {
                                    output = new DoubleExample(Double.parseDouble(example.toString()));
                                }else if (property.getFormat().equals("float")) {
                                    output = new FloatExample(Float.parseFloat(example.toString()));
                                }
                            }else{
                                output = new DecimalExample(new BigDecimal(example.toString()));
                            }
                        } catch (NumberFormatException e) {
                        }
                    }

                    if (output == null) {
                        BigDecimal defaultValue = ((NumberSchema) property).getDefault();

                        if (defaultValue == null) {
                            List<BigDecimal> enums = ((NumberSchema) property).getEnum();
                            if (enums != null && !enums.isEmpty()) {
                                defaultValue = enums.get(0);
                            }
                        }
                        if (property.getFormat() != null) {
                            if (property.getFormat().equals("double")) {
                                output = new DoubleExample(defaultValue == null ? SAMPLE_DOUBLE_PROPERTY_VALUE : defaultValue.doubleValue());
                            }
                            if (property.getFormat().equals("float")) {
                                output = new FloatExample(defaultValue == null ? SAMPLE_FLOAT_PROPERTY_VALUE : defaultValue.floatValue());
                            }
                        }else {
                            output = new DecimalExample(new BigDecimal(SAMPLE_DECIMAL_PROPERTY_VALUE));
                        }
                    }

        } else if (property instanceof BooleanSchema) {
            if (example != null) {
                output = new BooleanExample(Boolean.valueOf(example.toString()));
            }
            else {
                Boolean defaultValue = (Boolean) property.getDefault();
                output = new BooleanExample( defaultValue == null ? SAMPLE_BOOLEAN_PROPERTY_VALUE : defaultValue.booleanValue());
            }
        } else if (property instanceof DateSchema) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {

                List<Date> enums = ((DateSchema) property).getEnum();
                if( enums != null && !enums.isEmpty()) {
                    output = new StringExample(enums.get(0).toString());
                }
                else {
                    output = new StringExample(SAMPLE_DATE_PROPERTY_VALUE);
                }
            }
        } else if (property instanceof DateTimeSchema) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {
                List<Date> enums = ((DateTimeSchema) property).getEnum();
                if( enums != null && !enums.isEmpty()) {
                    output = new StringExample(enums.get(0).toString());
                }
                else {
                    output = new StringExample(SAMPLE_DATETIME_PROPERTY_VALUE);
                }
            }
        } else if (property instanceof ObjectSchema) {
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
                ObjectSchema op = (ObjectSchema) property;
                if(op.getProperties() != null) {
                    for(String propertyName : op.getProperties().keySet()) {
                        Schema inner = op.getProperties().get(propertyName);
                        Example innerExample = fromProperty(inner, definitions);
                        outputExample.put(propertyName, innerExample);
                    }
                }
                output = outputExample;
            }
        } else if (property instanceof ArraySchema) {
            if (example != null) {
                output = new ArrayExample();
            }
            else {
                ArraySchema ap = (ArraySchema) property;
                Schema inner = ap.getItems();
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
            }
        } else if (property.getAdditionalProperties() != null) {
            Schema inner = property.getAdditionalProperties();
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
        if (property.get$ref() != null && output == null) {
            if( definitions != null ) {
                String ref = property.get$ref();
                ref = ref.substring(ref.lastIndexOf("/") + 1);
                Schema model = definitions.get(ref);
                if (model != null) {
                    if (model.getXml() != null) {
                        XML xml = model.getXml();
                        name = xml.getName();
                        attribute = xml.getAttribute();
                        namespace = xml.getNamespace();
                        prefix = xml.getPrefix();
                        wrapped = xml.getWrapped();
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

                        Map<String, Schema> properties = model.getProperties();
                        if (properties != null) {
                            for (String key : properties.keySet()) {
                                Schema innerProp = properties.get(key);
                                Example p = fromProperty(innerProp, definitions, processedModels);
                                if (p != null) {
                                    if (p.getName() == null) {
                                        p.setName(key);
                                    }
                                    values.put(key, p);
                                    processedModels.add(key);
                                }
                            }
                        }
                        output = values;
                    }
                }
                if (output != null) {
                    output.setName(ref);
                }
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

    public static Example alreadyProcessedRefExample(String name, Map<String, Schema> definitions) {
        Schema model = definitions.get(name);
        if(model == null) {
            return null;
        }
        Example output = null;

        // look at type
        if(model.getType() != null) {
            if ("object".equals(model.getType())) {
                return new ObjectExample();
            }
            else if("string".equals(model.getType())) {
                return new StringExample("");
            }
            else if("integer".equals(model.getType())) {
                return new IntegerExample(0);
            }
            else if("long".equals(model.getType())) {
                return new LongExample(0);
            }
            else if("float".equals(model.getType())) {
                return new FloatExample(0);
            }
            else if("double".equals(model.getType())) {
                return new DoubleExample(0);
            }
        }


        return output;
    }

    public static Example fromModel(String name, Schema model, Map<String, Schema> definitions, Set<String> processedModels) {
        String namespace = null;
        String prefix = null;
        Boolean attribute = false;
        Boolean wrapped = false;

        Example output = null;
        if (model.getExample() != null) {
            try {
                String str = model.getExample().toString();
                output = Json.mapper().readValue(str, ObjectExample.class);
            } catch (IOException e) {
                return null;
            }
        }

        if (model.getXml() != null) {
            XML xml = model.getXml();
            name = xml.getName();
            namespace = xml.getNamespace();
            prefix = xml.getPrefix();
            attribute = xml.getAttribute();
            wrapped = xml.getWrapped() != null ? xml.getWrapped() : false;
        }

        ObjectExample ex = new ObjectExample();

        if(model.getProperties() != null) {
            Map<String,Schema> properties = model.getProperties();
            for(String key : properties.keySet()) {
                Schema property = (Schema)model.getProperties().get(key);
                Example propExample = fromProperty(property, definitions, processedModels);
                ex.put(key, propExample);
            }
        }
        output = ex;

        if (model instanceof ComposedSchema) {
            ComposedSchema composedSchema = (ComposedSchema) model;
            if(composedSchema.getAllOf() != null) {

                List<Schema> models = composedSchema.getAllOf();
                ex = new ObjectExample();

                List<Example> innerExamples = new ArrayList<>();
                if (models != null) {
                    for (Schema im : models) {
                        Example innerExample = fromModel(null, im, definitions, processedModels);
                        if (innerExample != null) {
                            innerExamples.add(innerExample);
                        }
                    }
                }
                mergeTo(ex, innerExamples);
                output = ex;
            }
        }
        else if(model instanceof ArraySchema) {
            ArraySchema am = (ArraySchema) model;
            ObjectExample sample = new ObjectExample();

            Schema inner = am.getItems();
            if (inner != null) {
                Example innerExample = fromProperty(inner, definitions, processedModels);
                if (innerExample != null) {
                    ArrayExample an = new ArrayExample();
                    an.add(innerExample);
                    output = an;
                }
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

    public static void mergeTo(ObjectExample output, List<Example> examples) {
        for(Example ex : examples) {
            if(ex instanceof ObjectExample) {
                ObjectExample objectExample = (ObjectExample) ex;
                Map<String, Example> values = objectExample.getValues();
                if( values != null ) {
                    output.putAll(values);
                }
            }
        }
    }
}
