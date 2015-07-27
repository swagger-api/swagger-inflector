package io.swagger.samples.inflector.dropwizard;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.inflector.SwaggerInflector;
import io.swagger.inflector.config.Configuration;
import io.swagger.models.Swagger;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.EnumSet;
import java.util.Set;

public class InflectorServer extends Application<InflectorServerConfiguration> {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(InflectorServer.class);

    public static void main(String[] args) throws Exception {
        new InflectorServer().run(args);
    }

    @Override
    public String getName() {
        return "Inflector Dropwizard Sample";
    }

    @Override
    public void initialize(Bootstrap<InflectorServerConfiguration> bootstrap) {
    }

    @Override
    public void run(InflectorServerConfiguration configuration, Environment environment) throws Exception {
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("crossOriginRequsts", CrossOriginFilter.class);
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        SwaggerInflector inflector = new SwaggerInflector( Configuration.read(configuration.getConfig()) );
        environment.jersey().getResourceConfig().registerResources(inflector.getResources());
    }
}
