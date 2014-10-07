# vertx-jersey-jackson

This sample shows running jersey inside vert.x using the `org.glassfish.jersey.jackson.JacksonFeature` for json
serialization.

## Run It

1. Run from the command line `mvn vertx:runMod'
2. Run from inside IDEA via the vert.x maven plugin vertx:runMod


Try the following urls in your browser:
* `http://localhost:8080/json`
* `http://localhost:8080/json/async`
* `http://localhost:8080/json/jsonp`
* `http://localhost:8080/json/jsonp?cb=fooback`


## The Configuration

mod.json instructs vert.x to include the vertx-mod-jersey module and to run `com.englishtown.vertx.jersey.JerseyModule`
```json
{
    "main": "com.englishtown.vertx.jersey.JerseyModule",
    "includes": "com.englishtown~vertx-mod-jersey~3.0.0-SNAPSHOT"
}
```

config.json sets up the vertx-mod-jersey module with the following settings:
```json
{
    "host": "localhost",
    "port": 8080,
    "base_path": "/",
    "resources": ["com.englishtown.vertx.jersey.examples.resources"],
    "features": ["org.glassfish.jersey.jackson.JacksonFeature"]
}
```
