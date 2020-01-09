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
import io.swagger.models.ArrayModel;
import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.Xml;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BaseIntegerProperty;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public static Example fromProperty(Property property, Map<String, Model> definitions) {
        return fromProperty(property, definitions, new HashMap<String, Example>());
    }

    public static Example fromProperty(Property property, Map<String, Model> definitions, Set<String> processedModels) {
        return fromProperty(property, definitions, createNullFiledMap(processedModels));
    }

    public static Example fromProperty(Property property, Map<String, Model> definitions, Map<String, Example> processedModels) {
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
            if(processedModels.containsKey(ref.getSimpleRef())) {
                // return some sort of example
                return alreadyProcessedRefExample(ref.getSimpleRef(), definitions, processedModels);
            }
            processedModels.put(ref.getSimpleRef(), null);
            if( definitions != null ) {
                Model model = definitions.get(ref.getSimpleRef());
                if (model != null) {
                    output = fromModel(ref.getSimpleRef(), model, definitions, processedModels);
                    processedModels.put(ref.getSimpleRef(), output);
                }
            }
        } else if (property instanceof EmailProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {
                String defaultValue = ((EmailProperty)property).getDefault();

                if( defaultValue == null ){
                    List<String> enums = ((EmailProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new StringExample( defaultValue == null ? SAMPLE_EMAIL_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof UUIDProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {
                String defaultValue = ((UUIDProperty)property).getDefault();

                if( defaultValue == null ){
                    List<String> enums = ((UUIDProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new StringExample( defaultValue == null ? SAMPLE_UUID_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof StringProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            } else {
                String defaultValue = ((StringProperty)property).getDefault();

                if( defaultValue == null ){
                    List<String> enums = ((StringProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new StringExample( defaultValue == null ? SAMPLE_STRING_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof IntegerProperty) {
            if (example != null) {
                try {
                    output = new IntegerExample(Integer.parseInt(example.toString()));
                }
                catch( NumberFormatException e ){}
            }

            if( output == null )  {
                Integer defaultValue = ((IntegerProperty) property).getDefault();

                if( defaultValue == null ){
                    List<Integer> enums = ((IntegerProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new IntegerExample( defaultValue == null ? SAMPLE_INT_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof LongProperty) {
            if (example != null) {
                try {
                    output = new LongExample(Long.parseLong(example.toString()));
                }
                catch( NumberFormatException e ) {}
            }

            if( output == null ) {
                Long defaultValue = ((LongProperty) property).getDefault();

                if( defaultValue == null ){
                    List<Long> enums = ((LongProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new LongExample( defaultValue == null ? SAMPLE_LONG_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof BaseIntegerProperty) {
            if (example != null) {
                try {
                    output = new IntegerExample(Integer.parseInt(example.toString()));
                }
                catch( NumberFormatException e ){}
            }

            if( output == null ) {
                output = new IntegerExample(SAMPLE_BASE_INTEGER_PROPERTY_VALUE);
            }
        } else if (property instanceof FloatProperty) {
            if (example != null) {
                try {
                    output = new FloatExample(Float.parseFloat(example.toString()));
                }
                catch( NumberFormatException e ){}
            }

            if( output == null ) {
                Float defaultValue = ((FloatProperty) property).getDefault();

                if( defaultValue == null ){
                    List<Float> enums = ((FloatProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new FloatExample( defaultValue == null ? SAMPLE_FLOAT_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof DoubleProperty) {
            if (example != null) {
                try {
                    output = new DoubleExample(Double.parseDouble(example.toString()));
                }
                catch( NumberFormatException e ){}
            }

            if( output == null ){
                Double defaultValue = ((DoubleProperty) property).getDefault();

                if( defaultValue == null ){
                    List<Double> enums = ((DoubleProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new DoubleExample( defaultValue == null ? SAMPLE_DOUBLE_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof BooleanProperty) {
            if (example != null) {
                output = new BooleanExample(Boolean.valueOf(example.toString()));
            }
            else {
                Boolean defaultValue = ((BooleanProperty)property).getDefault();
                output = new BooleanExample( defaultValue == null ? SAMPLE_BOOLEAN_PROPERTY_VALUE : defaultValue.booleanValue());
            }
        } else if (property instanceof DateProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {

                List<String> enums = ((DateProperty) property).getEnum();
                if( enums != null && !enums.isEmpty()) {
                    output = new StringExample(enums.get(0));
                }
                else {
                    output = new StringExample(SAMPLE_DATE_PROPERTY_VALUE);
                }
            }
        } else if (property instanceof DateTimeProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {
                List<String> enums = ((DateTimeProperty) property).getEnum();
                if( enums != null && !enums.isEmpty()) {
                    output = new StringExample(enums.get(0));
                }
                else {
                    output = new StringExample(SAMPLE_DATETIME_PROPERTY_VALUE);
                }
            }
        } else if (property instanceof DecimalProperty) {
            if (example != null) {
                try {
                    output = new DecimalExample(new BigDecimal(example.toString()));
                }
                catch( NumberFormatException e ){}
            }

            if( output == null ){
                output = new DecimalExample(new BigDecimal(SAMPLE_DECIMAL_PROPERTY_VALUE));
            }
        } else if (property instanceof ObjectProperty) {
            if(processedModels.containsKey(property.getName())) {
                // return some sort of example
                return alreadyProcessedRefExample(property.getName(), definitions, processedModels);
            }
            if (example != null) {
                try {
                    output = Json.mapper().readValue(Json.mapper().writeValueAsString(example), ObjectExample.class);
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
                        Example innerExample = fromProperty(inner, definitions, processedModels);
                        outputExample.put(propertyname, innerExample);
                    }
                }
                output = outputExample;
            }
        } else if (property instanceof ArrayProperty) {
            if (example != null) {
                try {
                    output = Json.mapper().readValue(Json.mapper().writeValueAsString(example), ArrayExample.class);
                } catch (IOException e) {
                    LOGGER.error("unable to convert `" + example + "` to JsonNode");
                    output = new ArrayExample();
                }
            }
            else {
                ArrayProperty ap = (ArrayProperty) property;
                Property inner = ap.getItems();
                if (inner != null) {
                    Object innerExample = fromProperty(inner, definitions, processedModels);
                    if (innerExample != null) {
                        if (innerExample instanceof Example) {
                            if (ap.getXml() == null || Boolean.FALSE.equals(ap.getXml().getWrapped())) {
                                ((Example) innerExample).setName(null);
                            }
                            ArrayExample an = new ArrayExample();
                            an.add((Example) innerExample);
                            an.setName(property.getName());
                            output = an;
                        }
                    }
                }
            }
        } else if (property instanceof MapProperty) {
            MapProperty mp = (MapProperty) property;
            Property inner = mp.getAdditionalProperties();
            if (inner != null) {
                ObjectExample on = new ObjectExample();
                for (int i = 1; i <= 3; i++) {
                    Example innerExample = fromProperty(inner, definitions, processedModels);
                    if (innerExample != null) {
                        String key = "additionalProp" + i;
                        on.put(key, innerExample);
                        output = on;
                    }
                }
            }
        }

        // TODO: File
        if (property instanceof RefProperty && output == null) {
            if( definitions != null ) {
                RefProperty ref = (RefProperty) property;
                Model model = definitions.get(ref.getSimpleRef());
                if (model != null) {
                    output = fromModel(ref.getSimpleRef(), model, definitions, processedModels);
                }
                if (output != null) {
                    output.setName(ref.getSimpleRef());
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


    public static Example alreadyProcessedRefExample(String name, Map<String, Model> definitions, Map<String, Example> processedExamples) {
        if (processedExamples.get(name) != null){
            return processedExamples.get(name);
        }

        Model model = definitions.get(name);
        if(model == null) {
            return null;
        }
        Example output = null;

        if(model instanceof ModelImpl) {
            // look at type
            ModelImpl impl = (ModelImpl) model;
            if(impl.getType() != null) {
                if ("object".equals(impl.getType())) {
                    return new ObjectExample();
                }
                else if("string".equals(impl.getType())) {
                    return new StringExample("");
                }
                else if("integer".equals(impl.getType())) {
                    return new IntegerExample(0);
                }
                else if("long".equals(impl.getType())) {
                    return new LongExample(0);
                }
                else if("float".equals(impl.getType())) {
                    return new FloatExample(0);
                }
                else if("double".equals(impl.getType())) {
                    return new DoubleExample(0);
                }
            }
        }

        return output;
    }

    public static Example fromModel(String name, Model model, Map<String, Model> definitions, Set<String> processedModels) {
        return fromModel(name, model, definitions, createNullFiledMap(processedModels));
    }

    private static Map<String, Example> createNullFiledMap(Set<String> processedModels) {
        Map<String, Example> processedModelsMap = new HashMap<>();
        if (processedModels != null) {
            for (String processedModel : processedModels) {
                processedModelsMap.put(processedModel, null);
            }
        }
        return processedModelsMap;
    }


    public static <T extends Number> Example fromNumberModel(ModelImpl impl, Class<T> clazz) {
        Object example = impl.getExample();
        Example output = null;
        if (example != null) {
            try {
                if(Integer.class.isAssignableFrom(clazz)) {
                    output = new IntegerExample(Integer.parseInt(example.toString()));
                } else if(Long.class.isAssignableFrom(clazz)) {
                    output = new LongExample(Long.parseLong(example.toString()));
                } else if(Double.class.isAssignableFrom(clazz)) {
                    output = new DoubleExample(Double.parseDouble(example.toString()));
                } else if(Float.class.isAssignableFrom(clazz)) {
                    output = new FloatExample(Float.parseFloat(example.toString()));
                } else {
                    output = new IntegerExample(Integer.parseInt(example.toString()));
                }
            } catch( NumberFormatException e ){}
        }
        if( output == null )  {
            T defaultValue = null;
            try {
                if(Integer.class.isAssignableFrom(clazz)) {
                    defaultValue = impl.getDefaultValue() == null ? null : (T)Integer.valueOf(defaultValue.toString());
                } else if(Long.class.isAssignableFrom(clazz)) {
                    defaultValue = impl.getDefaultValue() == null ? null : (T)Long.valueOf(defaultValue.toString());
                } else if(Double.class.isAssignableFrom(clazz)) {
                    defaultValue = impl.getDefaultValue() == null ? null : (T)Double.valueOf(defaultValue.toString());
                } else if(Float.class.isAssignableFrom(clazz)) {
                    defaultValue = impl.getDefaultValue() == null ? null : (T)Float.valueOf(defaultValue.toString());
                }

            } catch( Exception e ){}

            if( defaultValue == null ){
                List<String> enums = impl.getEnum();
                if( enums != null && !enums.isEmpty()) {
                    try {
                        if(Integer.class.isAssignableFrom(clazz)) {
                            defaultValue = impl.getDefaultValue() == null ? null : (T)Integer.valueOf(enums.get(0));
                        } else if(Long.class.isAssignableFrom(clazz)) {
                            defaultValue = impl.getDefaultValue() == null ? null : (T)Long.valueOf(enums.get(0));
                        } else if(Double.class.isAssignableFrom(clazz)) {
                            defaultValue = impl.getDefaultValue() == null ? null : (T)Double.valueOf(enums.get(0));
                        } else if(Float.class.isAssignableFrom(clazz)) {
                            defaultValue = impl.getDefaultValue() == null ? null : (T)Float.valueOf(enums.get(0));
                        }
                    } catch( Exception e ){}
                }
            }
            if(Integer.class.isAssignableFrom(clazz)) {
                output = new IntegerExample( defaultValue == null ? SAMPLE_INT_PROPERTY_VALUE : (Integer)defaultValue );
            } else if(Long.class.isAssignableFrom(clazz)) {
                output = new LongExample( defaultValue == null ? SAMPLE_LONG_PROPERTY_VALUE : (Long)defaultValue );
            } else if(Double.class.isAssignableFrom(clazz)) {
                output = new DoubleExample( defaultValue == null ? SAMPLE_DOUBLE_PROPERTY_VALUE : (Double)defaultValue );
            } else if(Float.class.isAssignableFrom(clazz)) {
                output = new FloatExample( defaultValue == null ? SAMPLE_FLOAT_PROPERTY_VALUE : (Float)defaultValue );
            }

        }
        return output;
    }

    public static Example fromModel(String name, Model model, Map<String, Model> definitions, Map<String, Example> processedModels) {
        if (model == null) {
            return null;
        }

        String namespace = null;
        String prefix = null;
        Boolean attribute = false;
        Boolean wrapped = false;

        Example output = null;
        Object example = model.getExample();
        if (model instanceof ModelImpl) {
            ModelImpl impl = (ModelImpl) model;
            String modelType = impl.getType() != null ? impl.getType() : "";

            if (impl.getXml() != null) {
                Xml xml = impl.getXml();
                name = xml.getName();
                namespace = xml.getNamespace();
                prefix = xml.getPrefix();
                attribute = xml.getAttribute();
                wrapped = xml.getWrapped() != null ? xml.getWrapped() : false;
            }

            ObjectExample ex = new ObjectExample();
            switch (modelType) {
                case "string":
                    if (example != null) {
                        output = new StringExample(example.toString());
                    } else {
                        String defaultValue = impl.getDefaultValue() == null ? null : impl.getDefaultValue().toString();
                        if( defaultValue == null ){
                            List<String> enums = impl.getEnum();
                            if( enums != null && !enums.isEmpty()) {
                                defaultValue = enums.get(0);
                            }
                        }
                        String samplePropertyValue = SAMPLE_STRING_PROPERTY_VALUE;
                        if (!StringUtils.isBlank(impl.getFormat())) {
                            switch (impl.getFormat()) {
                                case "email":
                                    samplePropertyValue = SAMPLE_EMAIL_PROPERTY_VALUE;
                                    break;
                                case "uuid":
                                    samplePropertyValue = SAMPLE_UUID_PROPERTY_VALUE;
                                    break;
                                case "date":
                                    samplePropertyValue = SAMPLE_DATE_PROPERTY_VALUE;
                                    break;
                                case "datetime":
                                    samplePropertyValue = SAMPLE_DATETIME_PROPERTY_VALUE;
                                    break;
                                default:
                                    samplePropertyValue = SAMPLE_STRING_PROPERTY_VALUE;
                            }
                        }

                        output = new StringExample( defaultValue == null ? samplePropertyValue : defaultValue );
                    }
                    break;
                case "integer":
                    if (StringUtils.isBlank(impl.getFormat())) {
                        output = fromNumberModel(impl, Integer.class);
                    } else {
                        switch (impl.getFormat()) {
                            case "int32":
                                output = fromNumberModel(impl, Integer.class);
                                break;
                            case "int64":
                                output = fromNumberModel(impl, Long.class);
                                break;
                            default:
                                output = fromNumberModel(impl, Integer.class);
                        }
                    }
                    break;
                case "number":
                    if (StringUtils.isBlank(impl.getFormat())) {
                        output = fromNumberModel(impl, Double.class);
                    } else {
                        switch (impl.getFormat()) {
                            case "double":
                                output = fromNumberModel(impl, Double.class);
                                break;
                            case "float":
                                output = fromNumberModel(impl, Float.class);
                                break;
                            default:
                                output = fromNumberModel(impl, Double.class);
                        }
                    }
                    break;
                case "boolean":
                    if (example != null) {
                        output = new BooleanExample(Boolean.valueOf(example.toString()));
                    } else {
                        Boolean defaultValue = impl.getDefaultValue() == null ? null : Boolean.valueOf(impl.getDefaultValue().toString());
                        output = new BooleanExample( defaultValue == null ? SAMPLE_BOOLEAN_PROPERTY_VALUE : defaultValue.booleanValue());
                    }
                    break;
                case "object":
                case "":
                default:
                    if (example != null) {
                        try {
                            output = Json.mapper().readValue(Json.mapper().writeValueAsString(example), ObjectExample.class);
                        } catch (IOException e) {
                            LOGGER.error("unable to convert `" + example + "` to JsonNode");
                            output = new ObjectExample();
                        }
                    } else {

                        if (impl.getProperties() != null) {
                            for (String key : impl.getProperties().keySet()) {
                                Property property = impl.getProperties().get(key);
                                if (property instanceof ObjectProperty) {
                                    property.setName(StringUtils.capitalize(key));
                                }
                                Example propExample = fromProperty(property, definitions, processedModels);
                                ex.put(key, propExample);
                            }
                        }

                        if (impl.getAdditionalProperties() != null) {
                            Property additionalProperties = impl.getAdditionalProperties();
                            for (int i = 1; i <= 3; i++) {
                                Example propExample = fromProperty(additionalProperties, definitions, processedModels);
                                String key = "additionalProp" + i;
                                if (propExample != null && !ex.keySet().contains(key)) {
                                    ex.put(key, propExample);
                                }
                            }
                        }
                        output = ex;
                    }
            }
        }
        else if(model instanceof ComposedModel) {
            if (example != null) {
                try {
                    output = Json.mapper().readValue(Json.mapper().writeValueAsString(example), ObjectExample.class);
                } catch (IOException e) {
                    LOGGER.error("unable to convert `" + example + "` to JsonNode");
                    output = new ObjectExample();
                }
            } else {

                ComposedModel cm = (ComposedModel) model;
                List<Model> models = cm.getAllOf();
                ObjectExample ex = new ObjectExample();

                List<Example> innerExamples = new ArrayList<>();
                if (models != null) {
                    for (Model im : models) {
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
        else if(model instanceof ArrayModel) {
            if (example != null) {
                try {
                    output = Json.mapper().readValue(Json.mapper().writeValueAsString(example), ArrayExample.class);
                } catch (IOException e) {
                    LOGGER.error("unable to convert `" + example + "` to JsonNode");
                    output = new ObjectExample();
                }
            } else {
                ArrayModel am = (ArrayModel) model;

                Property inner = am.getItems();
                if (inner != null) {
                    Example innerExample = fromProperty(inner, definitions, processedModels);
                    if (innerExample != null) {
                        ArrayExample an = new ArrayExample();
                        an.add(innerExample);
                        output = an;
                    }
                }
            }
        }
        else if(model instanceof RefModel) {
            RefModel ref = (RefModel) model;
            if(processedModels.containsKey(ref.getSimpleRef())) {
                // return some sort of example
                output = alreadyProcessedRefExample(ref.getSimpleRef(), definitions, processedModels);
            } else {
                processedModels.put(ref.getSimpleRef(), null);
                if (definitions != null) {
                    Model refedModel = definitions.get(ref.getSimpleRef());
                    if (refedModel != null) {
                        output = fromModel(ref.getSimpleRef(), refedModel, definitions, processedModels);
                        processedModels.put(ref.getSimpleRef(), output);
                    }
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
