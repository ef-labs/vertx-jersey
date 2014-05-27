# inject-securitycontext

This sample shows running jersey inside vert.x using a SecurityContextProvider to provide a custom SeurityContext.
It also demonstrates using the JAX-RS `@RolesAllowed` security annotation.

## Run It

1. Run from the command line `mvn vertx:runMod'
2. Run from inside IDEA via the vert.x maven plugin vertx:runMod


Try the following urls in your browser:
* `http://localhost:8080/test`
* `http://localhost:8080/test/secure` - 403 Forbidden
* `http://localhost:8080/test/secure?role=1` - 200 OK
* `http://localhost:8080/test/secure?role=3` - 403 Forbidden


## The Configuration

mod.json instructs vert.x to include the vertx-mod-jersey module and to run `com.englishtown.vertx.jersey.JerseyModule`
```json
{
    "main": "com.englishtown.vertx.jersey.JerseyModule",
    "includes": "com.englishtown~vertx-mod-jersey~2.5.0"
}
```

config.json sets up the vertx-mod-jersey module with the following settings:
```json
{
    "host": "localhost",
    "port": 8080,
    "base_path": "/",
    "resources": ["com.englishtown.vertx.jersey.examples.resources"],
    "features": ["org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature"],
    "binders": [],
    "hk2_binder": [
        "com.englishtown.vertx.hk2.BootstrapBinder",
        "com.englishtown.vertx.jersey.examples.SecurityContextBinder"
        ]
}
```
