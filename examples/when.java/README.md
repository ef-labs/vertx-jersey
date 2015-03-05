# chunked

This sample shows running jersey inside vert.x using when.java promises and `WhenJerseyServer`

## Run It

1. Run from the command line `mvn vertx:runMod'
2. Run from inside IDEA via the vert.x maven plugin vertx:runMod


Try the following url in your browser:
* `http://localhost:8080/when`


## The Configuration

mod.json instructs vert.x to include the vertx-mod-jersey and vertx-mod-when modules and to run `StartupVerticle`
```json
{
    "main": "com.englishtown.vertx.jersey.examples.StartupVerticle",
    "includes": "com.englishtown~vertx-mod-jersey~3.0.1-SNAPSHOT,com.englishtown~vertx-mod-when~3.0.0"
}
```

config.json sets up the vertx-mod-jersey module with the following settings:
```json
{
    "hk2_binder": [
        "com.englishtown.vertx.hk2.WhenHK2JerseyBinder",
        "com.englishtown.vertx.promises.hk2.HK2WhenBinder"
    ],

    "jersey": {
        "host": "localhost",
        "port": 8080,
        "base_path": "/",
        "resources": ["com.englishtown.vertx.jersey.examples.resources"]
    }
}
```

Note the hk2 bindings for Jersey and when.java.