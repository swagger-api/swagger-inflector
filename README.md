# Swagger Inflector

[![Build Status](https://travis-ci.org/swagger-api/swagger-inflector.svg?branch=master)](https://travis-ci.org/swagger-api/swagger-inflector)

This project uses the Swagger Specification to drive an API implementation.  Rather than a typical top-down or bottom-up swagger integration, the Inflector uses the swagger specification as a DSL for the REST API.  The spec drives the creation of routes and controllers automatically, matching methods and method signatures from the implementation.  This brings a similar integration approach to the JVM as [swagger-node](https://github.com/swagger-api/swagger-node) brings to the javascript world.

To allow for an iterative development, the framework will mock responses for any unimplemented methods, based on the specification.  That means you can ship your API to your consumers for review immediately as you build it out.

You have full control over the mapping of controllers to classes and methods as well as models.

## Quick start!

Run this command to start in a hurry.  It will create a project named `my-project`
```
curl -L http://bit.ly/1Pl62pe | project=my-project bash
```

This will download everything you need to start editing and running a swagger-inflector based project.  See the output of the command for instructions.

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
# mode (development | staging | production).  Default is development, and this value will be overridden by a system property
# -Denvironment=production for example
environment: development

# configure your default controller package for method discovery
controllerPackage: io.swagger.sample.controllers

# configure the default model package for model discovery
modelPackage: io.swagger.sample.models

# the path to the swagger definition (Note! this can be overridden with -DswaggerUrl as a system property
swaggerUrl: swagger.yaml

# specific mappings for models, used to locate models in the `#/definitions/${model}`
modelMappings:
  User: io.swagger.sample.models.User

# HTTP response code when required parameters are missing
invalidRequestCode: 400
```

### Locating the controller class

The actual controller class for each method is located via the first of the following mechanisms:
- a x-swagger-router-controller extension at the method level can specify the specific controller class
- each tag associated with the method is assembled into the classnames "&lt;controllerPackage&gt;.&lt;Tag&gt;" or 
"&lt;controllerPackage&gt;.&lt;Tag&gt;Controller", the first of these classes that is found by Class.forName(...) will be used
- an optional &lt;controllerClass&gt; configuration parameter is appended to &lt;controllerPackage&gt; 
- as a last resort a class named &lt;controllerPackage&gt;.Default is used

By default the class is loaded directly with Class.forName(...).newInstance() - but you can override class creation
by providing a custom ControllerFactory to the inflector configuration (for example if you want your controllers to be 
loaded by a DI framework).

### Locating the target method

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

#### Payload validation

Since your inbound and outbound payloads are defined with the Swagger schema, Inflector 
can validate them at runtime.  Just enable payload validations in your inflector config:

```yaml
validatePayloads: true
```

And at start-up, Inflector will read the schema and attach the relevant section of it 
to the operation.  For example, a post operation that has this as the schema definition:

```json
{
  "Category": {
    "required": [
      "id"
    ],
    "properties": {
      "id": {
        "type": "integer",
        "format": "int64"
      },
      "name": {
        "type": "string"
      }
    },
    "xml": {
      "name": "Category"
    }
  }
}
```

Will fail if the incoming body looks like this:

```json
{
  "name": "Tony"
}
```
becase the required field `id` is missing.

The same goes for responses generated by the server.  Any response code that you send will 
be validated against it's corresponding schema.

You can choose to enable this in development,
staging, or production.

#### Content type negotiation

There is a pluggable framework for handling different content types.  You can register any processor by the following:

```java
EntityProcessor myProcessor = new MyEntityProcessor();  // implements EntityProcessor
EntityProcessorFactory.addProcessor(myProcessor);
```

#### Development Lifecycle

There are three modes that the Inflector supports, as configured by the `environment` attribute in the inflector config:

 - **development**.  In this mode, mock responses will be sent for controllers which are not implemented.  The intention
   is to allow you to quickly iterate on the implementation of the design.  In addition, missing model implementations
   are tolerated and supported.

 - **staging**.  Warning messages will be logged when starting the service for any missing controller, method, or model.

 - **production**.  The expectation is all methods and declared (manually mapped) models exist.  If they don't, it'll throw
   nasty errors and the server will not start.

In development mode, there is a `/debug.json` page which shows implementation details of the inflector service.

If your Swagger Description is unparsable, the server will throw ugly errors on startup and the `debug.json` page will
   give indications as to why.

#### Samples

You can find samples for the inflector project in the [Swagger-Samples](https://github.com/swagger-api/swagger-samples) repository.  The inflector projects start with `inflector-`

---
<img src="http://swagger.io/wp-content/uploads/2016/02/logo.jpg"/>
