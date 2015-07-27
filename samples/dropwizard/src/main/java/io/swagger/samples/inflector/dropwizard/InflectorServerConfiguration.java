package io.swagger.samples.inflector.dropwizard;

import io.dropwizard.Configuration;

public class InflectorServerConfiguration extends Configuration {
   private String config;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
