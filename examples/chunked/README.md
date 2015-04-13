# chunked

This sample shows running jersey inside vert.x returning a chunked response (Transfer-Encoding: chunked) and a streamed response using `WriteStreamOutput`

## Run It

1. Run from the command line `java -jar target/vertx-jersey-examples-chunked-Version-fat.jar -conf src/test/resources/config.json'
2. Run from inside IDEA creating a JAR Application build configuration with program arguments `-conf src/test/resources/config.json`


Try the following urls in your browser:
* `http://localhost:8080/chunked`
* `http://localhost:8080/chunked/async`
* `http://localhost:8080/chunked/normal`
* `http://localhost:8080/chunked/stream`
