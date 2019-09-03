package io.swagger.oas.inflector.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.*;

@JsonPropertyOrder({ "valid", "swaggerDefinitionMessages", "unimplementedControllerCount", "unimplementedMethodCount", "unimplementedModelCount" })
public class InflectResult {
    private boolean valid;
    private List<String> swaggerDefinitionMessages;
    private Map<String, List<String>> unimplementedControllers;
    private Set<String> unimplementedModels;

    public InflectResult specParseMessage(String message) {
        if (swaggerDefinitionMessages == null) {
            swaggerDefinitionMessages = new ArrayList<>();
        }
        swaggerDefinitionMessages.add(message);
        return this;
    }

    public InflectResult valid() {
        this.valid = true;
        return this;
    }

    public InflectResult invalid() {
        this.valid = false;
        return this;
    }

    public InflectResult valid(boolean isValid) {
        this.valid = isValid;
        return this;
    }

    public InflectResult unimplementedModel(String model) {
        if (unimplementedModels == null) {
            unimplementedModels = new HashSet<String>();
        }
        unimplementedModels.add(model);
        return this;
    }

    public InflectResult unimplementedControllers(String location, List<String> messages) {
        for (String message : messages) {
            unimplementedController(location, message);
        }
        return this;
    }

    public InflectResult unimplementedController(String location, String message) {
        if (unimplementedControllers == null) {
            unimplementedControllers = new HashMap<String, List<String>>();
        }
        List<String> l = unimplementedControllers.get(location);
        if (l == null) {
            l = new ArrayList<>();
            unimplementedControllers.put(location, l);
        }
        l.add(message);

        return this;
    }

    public boolean isValid() {
        if(swaggerDefinitionMessages == null || swaggerDefinitionMessages.size() == 0) {
            return true;
        }
        return false;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Map<String, List<String>> getUnimplementedControllers() {
        return unimplementedControllers;
    }

    public void setUnimplementedControllers(Map<String, List<String>> unimplementedControllers) {
        this.unimplementedControllers = unimplementedControllers;
    }

    public Set<String> getUnimplementedModels() {
        return unimplementedModels;
    }

    public void setUnimplementedModels(Set<String> unimplementedModels) {
        this.unimplementedModels = unimplementedModels;
    }

    public List<String> getSwaggerDefinitionMessages() {
        return swaggerDefinitionMessages;
    }

    public void setSwaggerDefinitionMessages(List<String> swaggerDefinitionMessages) {
        this.swaggerDefinitionMessages = swaggerDefinitionMessages;
    }

    public Integer getUnimplementedModelCount() {
        if(unimplementedModels == null || this.unimplementedModels.size() == 0) {
            return null;
        }
        return this.unimplementedModels.size();
    }
    public Integer getUnimplementedControllerCount() {
        if(unimplementedControllers == null || unimplementedControllers.keySet().size() == 0) {
            return null;
        }
        return this.unimplementedControllers.keySet().size();
    }

    public Integer getUnimplementedMethodCount() {
        if(unimplementedControllers == null || unimplementedControllers.keySet().size() == 0) {
            return null;
        }
        int count = 0;
        for(String key : unimplementedControllers.keySet()) {
            count += unimplementedControllers.get(key).size();
        }
        return count;
    }
}
