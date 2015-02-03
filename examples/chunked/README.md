# chunked

This sample shows running jersey inside vert.x returning a chunked response (Transfer-Encoding: chunked)

## Run It

1. Run from the command line `java -jar target/vertx-jersey-examples-chunked-4.0.0-SNAPSHOT-fat.jar -conf src/test/resources/config.json'
2. Run from inside IDEA creating a JAR Application build configuration with program arguments `-conf src/test/resources/config.json`


Try the following urls in your browser:
* `http://localhost:8080/chunked`
* `http://localhost:8080/chunked/async`
* `http://localhost:8080/chunked/normal`
* `http://localhost:8080/chunked/stream`


## The Configuration

config.json sets up the vertx-jersey module with the following settings:
```json
{
    "host": "localhost",
    "port": 8080,
    "base_path": "/",
    "resources": ["com.englishtown.vertx.jersey.examples.resources"]
}
```
