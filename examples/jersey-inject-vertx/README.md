# jersey-inject-vertx

This sample shows running jersey inside vert.x using @Context to inject vert.x objects to a resource.

## Run It

To run this example inside IDEA, add a maven Run/Debug Configuration with the following command line setting:
`vertx:runMod -Dvertx.langs.java=com.englishtown~vertx-mod-hk2~1.6.0-SNAPSHOT:com.englishtown.vertx.hk2.HK2VerticleFactory`


Try the following urls in your browser:
* `http://localhost:8080/test`


## The Configuration

mod.json instructs vert.x to include the vertx-mod-jersey module and to run `com.englishtown.vertx.jersey.JerseyModule`
```json
{
    "main": "com.englishtown.vertx.jersey.JerseyModule",
    "includes": "com.englishtown~vertx-mod-jersey~2.5.0-SNAPSHOT"
}
```

config.json sets up the vertx-mod-jersey module with the following settings:
```json
{
    "host": "localhost",
    "port": 8080,
    "base_path": "/",
    "resources": ["com.englishtown.vertx.samples.resources"]
}
```
