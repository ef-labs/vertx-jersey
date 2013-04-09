[![Build Status](https://travis-ci.org/englishtown/vertx-mod-jersey.png)](https://travis-ci.org/englishtown/vertx-mod-jersey)

# vertx-mod-jersey

Allows creating JAX-RS jersey resources that will handle incoming http requests to vert.x.


## Configuration

The vertx-mod-jersey module configuration is as follows:

```json
{
    "host": <host>,
    "port": <port>,
    "base_path": <base_path>,
    "resources": [<resources>],
    "features": [<features>],
    "binders": [<binders>]
}
````

* `host` - The host or ip address to listen at for connections. `0.0.0.0` means listen at all available addresses.
Default is `0.0.0.0`
* `port` -  The port to listen at for connections. Default is `80`.
* `base_path` - The base path jersey responds to.  Default is `/`.
* `resources` - An array of package names to inspect for resources.
* `features` - An array of feature classes to inject.  For example: `"org.glassfish.jersey.jackson.JacksonFeature"`
* `binders` - An array of HK2 binder classes to configure injection bindings.


## Examples

### Simple

```json
{
    "resources": ["com.englishtown.vertx.jersey.resources"]
}
```

### All settings

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
