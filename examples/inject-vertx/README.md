# jersey-inject-vertx

This sample shows running jersey inside vert.x using @Context to inject vert.x objects to a resource.

## Run It

1. Run from the command line `mvn vertx:runMod'
2. Run from inside IDEA via the vert.x maven plugin vertx:runMod


Try the following urls in your browser:
* `http://localhost:8080/test`


## The Configuration

mod.json instructs vert.x to include the vertx-mod-jersey module and to run `com.englishtown.vertx.jersey.JerseyModule`
```json
{
    "main": "com.englishtown.vertx.jersey.JerseyModule",
    "includes": "com.englishtown~vertx-mod-jersey~2.6.0-SNAPSHOT"
}
```

config.json sets up the vertx-mod-jersey module with the following settings:
```json
{
    "host": "localhost",
    "port": 8080,
    "base_path": "/",
    "resources": ["com.englishtown.vertx.jersey.examples.resources"]
}
```
