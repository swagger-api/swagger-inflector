# Swagger Inflector

This project uses the Swagger Specification to drive an API implementation.  Rather than a typical top-down or bottom-up swagger integration, the Inflector uses the swagger specification as a DSL for the REST API.  The spec drives the creation of routes and controllers automatically, matching methods and method signatures for the implementation.  This brings a similar integration approach to the JVM as [swagger-node](https://github.com/swagger-api/swagger-node) brings to the javascript world.

To allow for an iterative development, the framework will mock responses for any unimplemented methods, based on the specification.  That means you can ship your API to your consumers for review immediately as you build it out.

You have full control over the mapping of controllers to classes and methods as well as models.

### Components

Inflector uses the following libraries:

 - Swagger models for the swagger definition
 - Jackson for JSON processing
 - Jersey 2.6 for REST

### Integration

Inflector will create routes and add them to Jersey.  You simply need to register the Inflector application in your webapp and it should be compatible with your existing deployment, whether with web.xml, spring, dropwizard, etc.

To add inflector via `web.xml`:

```xml
<servlet>
  <servlet-name>swagger-inflector</servlet-name>
  <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
  <init-param>
    <param-name>javax.ws.rs.Application</param-name>
    <param-value>io.swagger.inflector.SwaggerInflector</param-value>
  </init-param>
  <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
  <servlet-name>swagger-inflector</servlet-name>
  <url-pattern>/*</url-pattern>
</servlet-mapping>
```

This simply adds the `SwaggerInflector` application to Jersey.

### Configuration

Inflector uses a single yaml file for configuration.  The default file is `inflector.yaml` but it can be overridden by setting a system property when starting the JVM:

```
-Dconfig=/path/to/config
```

The configuration supports the following:

```yaml
# configure your default controller package for method discovery
controllerPackage: io.swagger.sample.controllers

# configure the default model package for model discovery
modelPackage: io.swagger.sample.models

# the path to the swagger definition
swaggerUrl: swagger.yaml

# specific mappings for models, used to locate models in the `#/definitions/${model}`
modelMappings:
  User: io.swagger.sample.models.User
```

When locating methods, the `operationId` is used as the method name for lookup via reflection.  If not specified, there is logic for generation of a method name.

Once a method is matched via name, the parameter types will be compared to ensure we have the right model.  In all methods, only java objects are supported--primitives currently will not match (this allows for proper nulls).

You can override a model mapping by setting a vendor extension in the swagger yaml:

```yaml
# uses method name, look for controllerPackage in the configuration
paths:
  /test1:
    get:
      x-swagger-router-controller: SampleController
      operationId: getTest1
      parameters:
        - name: id
          in: formData
          type: integer
          format: int64
        - name: name
          in: formData
          type: string
```

From the configuration example above, this will look for the following class:

```
class: io.swagger.sample.controllers.SampleController
```

with the following method:

```
method: public Object getTest1(java.lang.Integer id, java.lang.String name)
```

#### Complex inputs

When there are complex inputs, such as the example below:

```yaml
paths:
  /test2:
    post:
      x-swagger-router-controller: SampleController
      operationId: addUser
      parameters:
        - name: user
          in: body
          schema:
            $ref: '#/definitions/User'
        - name: name
          in: query
          type: string
```

the Inflector will do the following:

 - Look in the configuration for a mapping between `User` and a concrete class definition.  If the definition exists AND the class can be loaded, the method will look like such:

 ```
 public Object addUser(io.swagger.sample.models.User user, java.lang.String name)
 ```

 - If the definition does not exist, the `modelPackage` from the configuration will be used to attempt to load the class:

 ```
 // ref.getSimpleRef() returns only the `User` from `#/definitions/User`

 Class<?> cls = Class.forName(config.getModelPackage() + "." + ref.getSimpleRef())
 ```

 If the definition can be loaded it will be used as the method signature

 - If no model can be loaded, it is the developer's job to unwrap the input and parse it on their own.  This requires `Content-Type`-specific processing.  Inflector will then look for the following method:

 ```
 public Object addUser(JsonNode user, java.lang.String name)
 ```

 - If no method can be found, a mock response will be returned based on the swagger definition.  For complex objects, if an `example` exists, we will use that.  Otherwise, it will be constructed.

#### Outputs

TBD

#### Samples

There is a samples directory to show how to integrate with Inflector.  Before running any examples, you'll need to build the project and install it locally:

```
mvn install
```

##### jetty war with web.xml

This example uses a traditional web.xml file.  To run:

```
cd samples/jetty-webxml
mvn package jetty:run
```

This will load the configuration file `inflector.yaml` which points to a swagger configuration at `src/main/swagger/swagger.yaml`.  You can modify these files and the project will reload.

The swagger URL, as defined in the `swagger.yaml`, is hosted at `http://localhost:8080/v2/swagger.json` or `http://localhost:8080/v2/swagger.yaml`.

There is one controller implemented that maps the `addPet` operation.

