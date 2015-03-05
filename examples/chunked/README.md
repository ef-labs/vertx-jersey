# chunked

This sample shows running jersey inside vert.x returning a chunked response (Transfer-Encoding: chunked)

## Run It

1. Run from the command line `mvn vertx:runMod'
2. Run from inside IDEA via the vert.x maven plugin vertx:runMod


Try the following urls in your browser:
* `http://localhost:8080/chunked`
* `http://localhost:8080/chunked/async`
* `http://localhost:8080/chunked/normal`


## The Configuration

mod.json instructs vert.x to include the vertx-mod-jersey module and to run `com.englishtown.vertx.jersey.JerseyModule`
```json
{
    "main": "com.englishtown.vertx.jersey.JerseyModule",
    "includes": "com.englishtown~vertx-mod-jersey~3.0.1"
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
