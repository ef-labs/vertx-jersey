[![Build Status](https://travis-ci.org/englishtown/vertx-mod-jersey.png)](https://travis-ci.org/englishtown/vertx-mod-jersey)

# vertx-mod-jersey

Allows creating JAX-RS jersey resources that will handle incoming http requests to vert.x.


## @VertxParam Resource Injection

The com.englishtown.vertx.jersey.VertxParam annotation is used to inject vert.x parameters into a resource 
constructor, field, or method parameter.  Supported vert.x objects include
* org.vertx.java.core.Vertx
* org.vertx.java.platform.Container
* org.vertx.java.core.http.HttpServerRequest
* org.vertx.java.core.http.impl.HttpReadStreamBase
* org.vertx.java.core.streams.ReadStream

### Example Resource Method
```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public void getQuery(
        @Suspended final AsyncResponse response,
        @Context ContainerRequest jerseyRequest,
        @VertxParam HttpServerRequest vertxRequest,
        @VertxParam Vertx vertx,
        @VertxParam Container container) {

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
    "host": <host>,
    "port": <port>,
    "receive_buffer_size": <receive_buffer_size>,
    "max_body_size": <max_body_size>,
    "base_path": <base_path>,
    "resources": [<resources>],
    "features": [<features>],
    "binders": [<binders>]
}
````

* `host` - The host or ip address to listen at for connections. `0.0.0.0` means listen at all available addresses.
Default is `0.0.0.0`
* `port` -  The port to listen at for connections. Default is `80`.
* `receive_buffer_size` - The int receive buffer size.  The value is optional.
* `max_body_size` - The int max body size allowed.  The default value is 1MB.
* `base_path` - The base path jersey responds to.  Default is `/`.
* `resources` - An array of package names to inspect for resources.
* `features` - An array of feature classes to inject.  For example: `"org.glassfish.jersey.jackson.JacksonFeature"`
* `binders` - An array of HK2 binder classes to configure injection bindings.

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
