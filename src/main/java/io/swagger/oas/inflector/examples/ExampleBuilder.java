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

import io.swagger.oas.inflector.Constants;
import io.swagger.oas.inflector.examples.models.ArrayExample;
import io.swagger.oas.inflector.examples.models.BooleanExample;
import io.swagger.oas.inflector.examples.models.DecimalExample;
import io.swagger.oas.inflector.examples.models.DoubleExample;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.examples.models.FloatExample;
import io.swagger.oas.inflector.examples.models.IntegerExample;
import io.swagger.oas.inflector.examples.models.LongExample;
import io.swagger.oas.inflector.examples.models.NullExample;
import io.swagger.oas.inflector.examples.models.ObjectExample;
import io.swagger.oas.inflector.examples.models.StringExample;

import io.swagger.util.Json;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.EmailSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.PasswordSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;
import io.swagger.v3.oas.models.media.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

public class ExampleBuilder {

    public enum RequestType {
        READ, WRITE
    }

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

    public static Example fromSchema(Schema property, Map<String, Schema> definitions) {
        return fromProperty(null, property, definitions, new HashMap<>(), null);
    }

    public static Example fromSchema(Schema property, Map<String, Schema> definitions, boolean nullExample, boolean processNullExampleExtension) {
        return fromProperty(null, property, definitions, new HashMap<>(), null, nullExample, processNullExampleExtension);
    }

    public static Example fromSchema(Schema property, Map<String, Schema> definitions, RequestType requestType) {
        return fromProperty(null,property, definitions, new HashMap<>(), requestType);
    }

    public static Example fromSchema(Schema property, Map<String, Schema> definitions, RequestType requestType, boolean nullExample, boolean processNullExampleExtension) {
        return fromProperty(null,property, definitions, new HashMap<>(), requestType, nullExample, processNullExampleExtension);
    }


    public static Example fromProperty(String name, Schema property, Map<String, Schema> definitions, Set<String> processedModels, RequestType requestType) {
        Map<String, Example> map = new HashMap<>();
        for (String key : processedModels) {
            map.put(key, null);
        }
        return fromProperty(name, property, definitions, map, requestType);
    }

    public static Example fromProperty(String name, Schema property, Map<String, Schema> definitions, Map<String, Example> processedModels, RequestType requestType) {
        return fromProperty(name, property, definitions, processedModels, requestType, false, false);
    }

    public static Example fromProperty(
            String name,
            Schema property,
            Map<String, Schema> definitions,
            Map<String, Example> processedModels,
            RequestType requestType,
            boolean nullExample,
            boolean processNullExampleExtension) {

        if (property == null) {
            return null;
        }
        if (property.getReadOnly() != null && property.getReadOnly() && requestType == RequestType.WRITE) {
            return null;
        }
        if (property.getWriteOnly() != null && property.getWriteOnly() && requestType == RequestType.READ) {
            return null;
        }

        //name = null;
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

        if (example == null && property.get$ref() == null) {
            if (nullExample) {
                return new NullExample();
            } else if (processNullExampleExtension) {
                if (property.getExtensions() != null && property.getExtensions().get(Constants.X_INFLECTOR_NULL_EXAMPLE) != null) {
                    return new NullExample();
                }
            } else if (property.getExampleSetFlag()) {
                return new NullExample();
            }
        }

        if (property.get$ref() != null) {
            String ref = property.get$ref();
            ref = ref.substring(ref.lastIndexOf("/") + 1);
            if(processedModels.containsKey(ref)) {
                // return some sort of example
                return alreadyProcessedRefExample(ref, definitions, processedModels);
            }
            processedModels.put(ref, null);
            if( definitions != null ) {
                Schema model = definitions.get(ref);
                if (model != null) {
                    output = fromProperty(ref, model, definitions, processedModels, requestType, nullExample, processNullExampleExtension);
                    processedModels.put(ref, output);
                    return output;
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

                output = new StringExample( defaultValue == null ? SAMPLE_UUID_PROPERTY_VALUE : defaultValue.toString() );
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
        } else if (property instanceof PasswordSchema) {
            if (example != null) {
                output = new StringExample(example.toString());
            } else {
                String defaultValue = ((PasswordSchema)property).getDefault();

                if( defaultValue == null ){
                    List<String> enums = ((PasswordSchema) property).getEnum();
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
                Number defaultValue = ((IntegerSchema) property).getDefault();

                if (defaultValue == null) {
                    List<Number> enums = ((IntegerSchema) property).getEnum();
                    if (enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }
                if (property.getFormat() != null) {
                    if (property.getFormat().equals("int32")) {
                        output = new IntegerExample(defaultValue == null ? SAMPLE_INT_PROPERTY_VALUE : defaultValue.intValue());
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
                Boolean defaultValue = (Boolean)property.getDefault();
                output = new BooleanExample( defaultValue == null ? SAMPLE_BOOLEAN_PROPERTY_VALUE : defaultValue.booleanValue());
            }
        } else if (property instanceof DateSchema) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (example != null) {
                String exampleAsString = format.format(example);
                output = new StringExample(exampleAsString);
            }
            else {

                List<Date> enums = ((DateSchema) property).getEnum();
                if( enums != null && !enums.isEmpty()) {
                    output = new StringExample(format.format(enums.get(0)));
                }
                else {
                    output = new StringExample(SAMPLE_DATE_PROPERTY_VALUE);
                }
            }
        } else if (property instanceof DateTimeSchema) {
            if (example != null) {
                String exampleAsString = example.toString();
                output = new StringExample(exampleAsString);
            }
            else {
                List<OffsetDateTime> enums = ((DateTimeSchema) property).getEnum();
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
                    for(String propertyname : op.getProperties().keySet()) {
                        Schema inner = op.getProperties().get(propertyname);
                        Example innerExample = fromProperty(null, inner, definitions,processedModels, requestType, nullExample, processNullExampleExtension);
                        outputExample.put(propertyname, innerExample);
                    }
                    output = outputExample;
                }

            }
        } else if (property instanceof ArraySchema) {
            if (example != null) {
                try {
                    output = Json.mapper().readValue(example.toString(), ArrayExample.class);
                } catch (IOException e) {
                    LOGGER.error("unable to convert `" + example + "` to JsonNode");
                    output = new ArrayExample();
                }
            }
            else {
                ArraySchema ap = (ArraySchema) property;
                Schema inner = ap.getItems();
                if (inner != null) {
                    Object innerExample = fromProperty(null,inner, definitions, processedModels,requestType, nullExample, processNullExampleExtension);
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
        } else if (property instanceof ComposedSchema) {
            //validate resolved validators if true send back the first property if false the actual code
            ComposedSchema composedSchema = (ComposedSchema) property;
            if(composedSchema.getAllOf() != null) {

                List<Schema> models = composedSchema.getAllOf();
                ObjectExample ex = new ObjectExample();

                List<Example> innerExamples = new ArrayList<>();
                if (models != null) {
                    for (Schema im : models) {
                        Example innerExample = fromProperty(null, im, definitions, processedModels, requestType, nullExample, processNullExampleExtension);
                        if (innerExample != null) {
                            innerExamples.add(innerExample);
                        }
                    }
                }
                mergeTo(ex, innerExamples);
                output = ex;
            }if(composedSchema.getAnyOf() != null) {

                List<Schema> models = composedSchema.getAnyOf();
                if (models != null) {
                    for (Schema im : models) {
                        Example innerExample = fromProperty(null, im, definitions, processedModels, requestType, nullExample, processNullExampleExtension);
                        if (innerExample != null) {
                            output = innerExample;
                            break;
                        }
                    }
                }
            } if(composedSchema.getOneOf() != null) {

                List<Schema> models = composedSchema.getOneOf();

                if (models != null) {
                    for (Schema im : models) {
                        Example innerExample = fromProperty(null, im, definitions, processedModels, requestType, nullExample, processNullExampleExtension);
                        if (innerExample != null) {
                            output = innerExample;
                            break;
                        }
                    }
                }
            }
        }else if (property.getProperties() != null && output == null ){
            if (example != null) {
                try {
                    output = Json.mapper().readValue(example.toString(), ObjectExample.class);
                } catch (IOException e) {
                    LOGGER.error("unable to convert `" + example + "` to JsonNode");
                    output = new ObjectExample();
                }
            }
            else {
                ObjectExample ex = new ObjectExample();

                if(property.getProperties() != null) {
                    Map<String,Schema> properties = property.getProperties();
                    for(String propertyKey : properties.keySet()) {
                        Schema inner = properties.get(propertyKey);
                        Example propExample = fromProperty(null, inner, definitions, processedModels,requestType, nullExample, processNullExampleExtension);
                        ex.put(propertyKey, propExample);
                    }
                }

                output = ex;
            }

        }
        if (property.getAdditionalProperties() instanceof Schema) {
            Schema inner = (Schema) property.getAdditionalProperties();
            if (inner != null) {
                for (int i = 1; i <= 3; i++) {
                    Example innerExample = fromProperty(null, inner, definitions, processedModels, requestType, nullExample, processNullExampleExtension);
                    if (innerExample != null) {
                        if (output == null) {
                            output = new ObjectExample();
                        }
                        ObjectExample on = (ObjectExample) output;
                        String key = "additionalProp" + i;
                        if (innerExample.getName() == null) {
                            innerExample.setName(key);
                        }

                        if (!on.keySet().contains(key)) {
                            on.put(key, innerExample);
                        }
                    }
                }
            }
        } else if (property.getAdditionalProperties() instanceof Boolean && output == null) {
            output = new ObjectExample();
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

    public static Example alreadyProcessedRefExample(String name, Map<String, Schema> definitions, Map<String, Example> processedModels) {
        if (processedModels.get(name) != null) {
            return processedModels.get(name);
        }
        Schema model = definitions.get(name);
        if (model == null) {
            return null;
        }
        Example output = null;

        // look at type
        if (model.getType() != null) {
            if ("object".equals(model.getType())) {
                return new ObjectExample();
            } else if ("string".equals(model.getType())) {
                return new StringExample("");
            } else if ("integer".equals(model.getType())) {
                return new IntegerExample(0);
            } else if ("long".equals(model.getType())) {
                return new LongExample(0);
            } else if ("float".equals(model.getType())) {
                return new FloatExample(0);
            } else if ("double".equals(model.getType())) {
                return new DoubleExample(0);
            }
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
