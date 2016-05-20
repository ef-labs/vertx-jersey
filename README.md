[![Build Status](https://travis-ci.org/englishtown/vertx-jersey.png)](https://travis-ci.org/englishtown/vertx-jersey)

# vertx-jersey

Allows creating JAX-RS [Jersey](https://jersey.java.net/) resources in vert.x.


## Getting started

Add the vertx-jersey dependency to your project

```xml
<dependency>
    <groupId>com.englishtown.vertx</groupId>
    <artifactId>vertx-jersey</artifactId>
    <version>4.4.2</version>
</dependency>
```

See the [example modules](examples) for more details.

Version Matrix

vert.x    | vertx-jersey
--------- | ------------
3.2.1     | 4.4.2
3.2.0     | 4.3.0
3.1.0     | 4.2.0
3.0.0     | 4.0.0
2.x       | 3.0.1 (vertx-mod-jersey)




There are multiple ways to start the Jersey Server:

##### 1. Run vertx-jersey as a service 

Running as a service is probably the easiest way to get started.

From the command line:

```
vertx run service:com.englishtown.vertx:vertx-jersey:4.0.0-SNAPSHOT -conf config.json
```

Programmatically:

```java
vertx.deployVerticle("service:com.englishtown.vertx:vertx-jersey:4.0.0-RC2", config);
```

See the [maven-service](https://github.com/englishtown/vertx-jersey/tree/develop/examples/maven-service) example.  

NOTE: When running as a service, [vertx-hk2](https://github.com/englishtown/vertx-hk2) __must__ be on the class path.

##### 2. Run the verticle

Rather than running as a service, you can run the JerseyVerticle from the command line:

```
vertx run java-hk2:com.englishtown.vertx.jersey.JerseyVerticle -conf config.json
vertx run java-guice:com.englishtown.vertx.jersey.JerseyVerticle -conf config.json
```

Or programmatically

```java
vertx.deployVerticle("java-hk2:com.englishtown.vertx.jersey.JerseyVerticle", config);
vertx.deployVerticle("java-guice:com.englishtown.vertx.jersey.JerseyVerticle", config);
```

This assumes you have vertx-hk2 or vertx-guice on the class path as well as vertx-jersey and all its dependencies.


##### 3. Create JerseyServer yourself

You can also skip the `JerseyVerticle` and instantiate the `JerseyServer` yourself.  It is easiest to use DI for this, but it can also be done manually.


## Configuration

The vertx-jersey configuration is as follows:

```json
{
    "host": "<host>",
    "port": <port>,
    "ssl": <ssl>,
    "base_path": "<base_path>",
    "packages": ["<packages>"],
    "components": ["<components>"],
    "instances": ["<instances>"],
    "properties": {"<properties>"},
    "compression_supported": <compression_supported>,
    "jks_options": <jks_options>,
    "receive_buffer_size": <receive_buffer_size>,
    "max_body_size": <max_body_size>,
    "backlog_size": <backlog_sze>
}
````

* `host` - The host or ip address to listen at for connections. `0.0.0.0` means listen at all available addresses.
Default is `0.0.0.0`
* `port` -  The port to listen at for connections. Default is `80`.
* `ssl` - Should the server use `https` as a protocol? Default is `false`.
* `base_path` - The base path jersey responds to.  Default is `/`.
* `packages` - An array of package names to inspect for resources.  Can also use json field `resources`.
* `components` - An array of components class names to inject (features, etc.).  For example: `"org.glassfish.jersey.jackson.JacksonFeature"`.  Can also use json field `features`.
* `instances` - An array of singleton instances to be created and registered (HK2 binders etc.).  Can also use json field `binders`.
* `properties` - An object with additional properties to be set on the ResourceConfig.  Can also use json field `resource_config`.
* `compression_supported` - A boolean whether the server supports compression. Default is `false`.
* `jks_options` - A JSON object to create the io.vertx.core.net.JksOptions. Only used if `ssl` is `true`.
* `receive_buffer_size` - The int receive buffer size.  The value is optional.
* `max_body_size` - The int max body size allowed.  The default value is 1MB.
* `backlog_size` - An int that sets the http server backlog size.  The default value is 10,000

You must configure at least one package or component.

You can also group the jersey configuration under a `jersey` json field:

```
{
    "jersey": {
        "host": "<host>",
        "packages": "<packages>"
        ....
    }
}
```

#### Examples
##### Simple

```json
{
    "resources": ["com.englishtown.vertx.jersey.resources"]
}
```

##### All settings

```json
{
    "host": "localhost",
    "port": 8080,
    "base_path": "/rest",
    "resources": ["com.englishtown.vertx.jersey.resources", "com.englishtown.vertx.jersey.resources2"],
    "features": ["org.glassfish.jersey.jackson.JacksonFeature"],
    "binders": ["com.englishtown.vertx.jersey.AppBinder"]
}
```


## Vertx Resource Injection

The `javax.ws.rs.core.Context` annotation can be used to inject vert.x objects into a resource constructor, field,
or method parameter.  Supported vert.x objects include

* `io.vertx.core.http.HttpServerRequest`
* `io.vertx.core.http.HttpServerResponse`
* `io.vertx.core.streams.ReadStream<io.vertx.core.http.HttpServerRequest>`
* `io.vertx.core.Vertx`

To inject custom objects, you must provide one or more binders in the configuration.  See the injection example projects.


### Dependency Injection
The JerseyVerticle requires dependency injection.  Guice and HK2 binders are provided:

* `com.englishtown.vertx.guice.GuiceJerseyBinder`
* `com.englishtown.vertx.hk2.HK2JerseyBinder`

See the examples directory for runnable hk2 and guice samples.

###### vertx-guice
If using [vertx-guice](https://github.com/englishtown/vertx-guice), ensure the vertx-guice jar is on the class path so vert.x registers the `GuiceVerticleFactory`.

__Note: The Guice Multibindings extension is required.__

###### vertx-mod-hk2
If using [vertx-hk2](https://github.com/englishtown/vertx-hk2), ensure the vertx-hk2 jar is on the class path so vert.x registers the `HK2VerticleFactory`.

__Note: if you are using vertx-mod-hk2, ensure you are using 1.7.0 or higher.__


#### Example Resource Method
```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public void getQuery(
        @Suspended final AsyncResponse response,
        @Context ContainerRequest jerseyRequest,
        @Context HttpServerRequest vertxRequest,
        @Context Vertx vertx) {

    vertx.runOnLoop(new Handler<Void>() {
        @Override
        public void handle(Void aVoid) {
            response.resume("Hello World!");
        }
    });
}
```


## Promises

The promises package provides when.java wrappers to create a JerseyServer.  You must provide the when.java dependency.

#### Example

The following example assumes a `com.englishtown.vertx.jersey.promises.WhenJerseyServer` instance has been injected using the `com.englishtown.vertx.hk2.WhenHK2JerseyBinder` with vertx-hk2 module.

```java

    @Override
    public void start(Future<Void> startedResult) throws Exception {

        JsonObject jerseyConfig = vertx.getOrCreateContext().config().getJsonObject("jersey");

        jerseyServer.createServer(jerseyConfig)
                .then(server -> {
                    startedResult.complete();
                    return null;
                })
                .otherwise(t -> {
                    startedResult.fail(t);
                    return null;
                });

    }

```
