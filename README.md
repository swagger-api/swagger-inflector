# Swagger Inflector

[![Build Status](https://travis-ci.org/swagger-api/swagger-inflector.svg?branch=master)](https://travis-ci.org/swagger-api/swagger-inflector)

This project uses the Swagger Specification to drive an API implementation.  Rather than a typical top-down or bottom-up swagger integration, the Inflector uses the swagger specification as a DSL for the REST API.  The spec drives the creation of routes and controllers automatically, matching methods and method signatures from the implementation.  This brings a similar integration approach to the JVM as [swagger-node](https://github.com/swagger-api/swagger-node) brings to the javascript world.

To allow for an iterative development, the framework will mock responses for any unimplemented methods, based on the specification.  That means you can ship your API to your consumers for review immediately as you build it out.

You have full control over the mapping of controllers to classes and methods as well as models.

## Quick start!

Run this command to start in a hurry.  It will create a project named `my-project`
```
curl -L http://bit.ly/1MY50zo | project=my-project bash
```

This will download everything you need to start editing and running a swagger-inflector based project.  See the output of the command for instructions.

**This project is in preview status!**

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

# HTTP response code when required parameters are missing
invalidRequestCode: 400
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
      responses:
        200:
          description: Success!
```

From the configuration example above, this will look for the following class:

```
class: io.swagger.sample.controllers.SampleController
```

with the following method:

```
method: public Object getTest1(
    io.swagger.inflector.models.RequestContext,
    java.lang.Integer id,
    java.lang.String name)
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
      responses:
        200:
          description: Success!
```

the Inflector will do the following:

 - Look in vendor extensions for the models to see if a mapping exists.  If so, it will attempt to load it via the classloader

 ```yaml
   Address:
    x-swagger-router-model: io.swagger.test.models.Address
    properties:
      street:
        type: string
        example: 12345 El Monte Road
      city:
        type: string
        example: Los Altos Hills
      state:
        type: string
        example: CA
      zip:
        type: string
        example: '94022'
 ```

 - Look in the configuration for a mapping between `User` and a concrete class definition.  If the definition exists AND the class can be loaded, the method will look like such:

 ```
 public ResponseContext addUser (
    RequestContext context,             // request context
    io.swagger.sample.models.User user, // user being added
    java.lang.String name)              // the `name` query param
 ```

 - If the definition does not exist, the `modelPackage` from the configuration will be used to attempt to load the class:

 ```
 // ref.getSimpleRef() returns only the `User` from `#/definitions/User`

 Class<?> cls = Class.forName(config.getModelPackage() + "." + ref.getSimpleRef())
 ```

 If the definition can be loaded it will be used as the method signature

 - If no model can be loaded, it is the developer's job to unwrap the input and parse it on their own.  This requires `Content-Type`-specific processing.  Inflector will then look for the following method:

 ```
 public ResponseContext addUser (
    RequestContext context,             // request context
    JsonNode user,                      // a Json tree representing the user
    java.lang.String name)              // the `name` query param
 ```

 - If no method can be found, a mock response will be returned based on the swagger definition.  For complex objects, if an `example` exists, we will use that.  Otherwise, it will be constructed.


The RequestWrapper and ResponseContext contain information about headers (in and outbound), content-type and acceptable response types.

#### Outputs

Your controllers can return null (void response), an object (entity), or a `io.swagger.inflector.models.ResponseContext`, which allows you to send specific error codes, headers, and an optional entity.

For example, if you want to return a `Pet` from a controller:

```java
    public ResponseContext getPet(RequestContext request, java.lang.Integer petId) {
        // do your magic to fetch a pet...
        Pet pet = complexBusinessLogic.getPetById(petId);

        return new ResponseContext()
                .status(Status.OK)
                .entity(pet);
    }
```

and the Inflector will return a `200` response code, marshalling the `Pet` object into the appropriate content type.

If you do not implement your controller, the Inflector will generate sample data based on your model definitions.  It will honor any examples that you have in the definitions, assuming they are compatible with the schema you declared.  For example, this definition:

```yaml
properties:
  street:
    type: "string"
    example: "12345 El Monte Blvd"
  city:
    type: "string"
    example: "Los Altos Hills"
  state:
    type: "string"
    example: "CA"
    minLength: 2
    maxLength: 2
  zip:
    type: "string"
    example: "94022"
xml:
  name: "address"
```

Will produce this example for a `Accept:application/json`:

```json
{
  "street" : "12345 El Monte Blvd",
  "city" : "Los Altos Hills",
  "state" : "CA",
  "zip" : "94022"
}
```

and `application/yaml`:

```yaml
street: "12345 El Monte Blvd"
city: "Los Altos Hills"
state: "CA"
zip: "94022"
```

and `application/xml`:

```xml
<address>
  <street>12345 El Monte Blvd</street>
  <city>Los Altos Hills</city>
  <state>CA</state>
  <zip>94022</zip>
</address>
```

#### Content type negotiation

There is a pluggable framework for handling different content types.  You can register any processor by the following:

```java
EntityProcessor myProcessor = new MyEntityProcessor();  // implements EntityProcessor
EntityProcessorFactory.addProcessor(myProcessor);
```

#### Samples

There is a samples directory to show how to integrate with Inflector.  Before running any examples, you'll need to build the project and install it locally:

```
mvn install
```

##### dropwizard

This example uses the popular dropwizard framework, which is programmatically configured without any xml nonsense.

```
cd samples/dropwizard
mvn package
java -jar target/inflector-dropwizard-sample-1.0-SNAPSHOT.jar server server.yml 
```

You can now access the server at `http://localhost:8080/v2/swagger.json`

##### jetty war with web.xml

This example uses a traditional web.xml file.  To run:

```
cd samples/jetty-webxml
mvn package jetty:run
```

This will load the configuration file `inflector.yaml` which points to a swagger configuration at `src/main/swagger/swagger.yaml`.  You can modify these files and the project will reload.

The swagger URL, as defined in the `swagger.yaml`, is hosted at `http://localhost:8080/v2/swagger.json` or `http://localhost:8080/v2/swagger.yaml`.

There is one controller implemented that maps the `addPet` operation.

