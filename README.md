[![Build Status](https://travis-ci.org/englishtown/vertx-mod-jersey.png)](https://travis-ci.org/englishtown/vertx-mod-jersey)

# vertx-mod-jersey

Allows creating JAX-RS jersey resources that will handle incoming http requests to vert.x.


## Vertx Resource Injection

The javax.ws.rs.core.Context annotation can be used to inject vert.x objects into a resource constructor, field,
or method parameter.  Supported vert.x objects include
* org.vertx.java.core.http.HttpServerRequest
* org.vertx.java.core.http.HttpServerResponse
* org.vertx.java.core.streams.ReadStream<org.vertx.java.core.http.HttpServerRequest>
* org.vertx.java.core.Vertx
* org.vertx.java.platform.Container

To inject custom objects, you must provide one or more binders in the configuration.  See the integration test test_postJson() for an example.


### Example Resource Method
```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public void getQuery(
        @Suspended final AsyncResponse response,
        @Context ContainerRequest jerseyRequest,
        @Context HttpServerRequest vertxRequest,
        @Context Vertx vertx,
        @Context Container container) {

    vertx.runOnLoop(new Handler<Void>() {
        @Override
        public void handle(Void aVoid) {
            response.resume("Hello World!");
        }
    });
}
```

## Configuration

The vertx-mod-jersey module configuration is as follows:

```json
{
    "host": "<host>",
    "port": <port>,
    "ssl": <ssl>,
    "key_store_password": <key_store_password>,
    "key_store_path": <key_store_path>,
    "receive_buffer_size": <receive_buffer_size>,
    "max_body_size": <max_body_size>,
    "base_path": "<base_path>",
    "resources": ["<resources>"],
    "features": ["<features>"],
    "binders": ["<binders>"],
    "backlog_size": <backlog_sze>
}
````

* `host` - The host or ip address to listen at for connections. `0.0.0.0` means listen at all available addresses.
Default is `0.0.0.0`
* `port` -  The port to listen at for connections. Default is `80`.
* `ssl`. Should the server use `https` as a protocol? Default is `false`.
* `key_store_password`. Password of Java keystore which holds the server certificate. Only used if `ssl` is `true`. Default is `wibble`.
* `key_store_path`. Path to keystore which holds the server certificate. Default is `server-keystore.jks`. Only used if `ssl` is `true`. *Don't put the keystore under your webroot!*.
* `receive_buffer_size` - The int receive buffer size.  The value is optional.
* `max_body_size` - The int max body size allowed.  The default value is 1MB.
* `base_path` - The base path jersey responds to.  Default is `/`.
* `resources` - An array of package names to inspect for resources.
* `features` - An array of feature classes to inject.  For example: `"org.glassfish.jersey.jackson.JacksonFeature"`
* `binders` - An array of HK2 binder classes to configure injection bindings.
* `backlog_size` - An int that sets the http server backlog size.  The default value is 10,000

### Examples
#### Simple

```json
{
    "resources": ["com.englishtown.vertx.jersey.resources"]
}
```

#### All settings

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

## How to use

Add vertx-mod-jersey as an include in your mod.json.

```json
{
    "includes": "com.englishtown~vertx-mod-jersey~3.0.1-SNAPSHOT"
}
```

The vertx-mod-jersey jar (plus its dependencies javax.ws.rs-api, javax.inject, jersey-server, etc.) should be added to your project with scope "provided".

```xml
<dependency>
    <groupId>com.englishtown</groupId>
    <artifactId>vertx-mod-jersey</artifactId>
    <version>3.0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

You have 3 ways to start the Jersey Server:

1. In your mod.json file, make the start Verticle JerseyModule (`"main": "com.englishtown.vertx.jersey.JerseyModule"`).
2. In your own Verticle specified in mod.json `"main"`, create an instance of the JerseyServer and initialize similarly to how JerseyModule does.
3. Use when.java and `com.englishtown.vertx.jersey.promises.WhenJerseyServer` to simplify the process.  See the [promises](#promises) section below.


Use #1 if you don't have anything else to do at application start.  Use #2 if you need to deploy other modules at start.

#### Dependency Injection
The JerseyModule requires dependency injection.  Guice and HK2 binders are provided:

* `com.englishtown.vertx.guice.BootstrapBinder`
* `com.englishtown.vertx.hk2.BootstrapBinder`

See the examples directory for working hk2 and guice samples.

##### vertx-mod-guice
If using vertx-mod-guice, ensure you have configured the GuiceVerticleFactory in langs.properties:
`java=com.englishtown~vertx-mod-guice~1.3.0-final:com.englishtown.vertx.guice.GuiceVerticleFactory`

Or by setting a system property:
`-Dvertx.langs.java=com.englishtown~vertx-mod-guice~1.3.0-final:com.englishtown.vertx.guice.GuiceVerticleFactory`

__Note: The Guice Multibindings extension is required.__

##### vertx-mod-hk2
If using vertx-mod-hk2, ensure you have configured the HK2VerticleFactory in langs.properties:
`java=com.englishtown~vertx-mod-hk2~1.7.0:com.englishtown.vertx.hk2.HK2VerticleFactory`

Or by setting a system property:
`-Dvertx.langs.java=com.englishtown~vertx-mod-hk2~1.7.0:com.englishtown.vertx.hk2.HK2VerticleFactory`

__Note: if you are using vertx-mod-hk2, ensure you are using 1.7.0 or higher.__


## Promises

Provides when.java wrappers to create a JerseyServer.  You must provide the when.java dependency.

### Example

The following example assumes a `com.englishtown.vertx.jersey.promises.WhenJerseyServer` instance has been injected using the `com.englishtown.vertx.hk2.WhenHK2JerseyBinder` and vertx-mod-hk2 module.

```java

    @Override
    public void start(final Future<Void> startedResult) {

        // Get the jersey server configuration
        JsonObject config = config.getObject("jersey"); new JsonObject()

        // Create the jersey server and set the startedResult
        whenJerseyServer.createServer(config).then(
                new FulfilledRunnable<JerseyServer>() {
                    @Override
                    public Promise<JerseyServer, Void> run(JerseyServer value) {
                        start();
                        startedResult.setResult(null);
                        return null;
                    }
                },
                new RejectedRunnable<JerseyServer>() {
                    @Override
                    public Promise<JerseyServer, Void> run(Value<JerseyServer> value) {
                        startedResult.setFailure(value.error);
                        return null;
                    }
                }
        );

    }

```
