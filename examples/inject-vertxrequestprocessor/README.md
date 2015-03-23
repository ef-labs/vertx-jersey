# inject-vertrequestprocessor

This sample shows running jersey inside vert.x using one or more injected VertRequestProcessor and VertxResponseProcessor classes to process requests before and after Jersey.

## Run It

1. Run from the command line `java -jar target/vertx-jersey-examples-inject-vertxrequestprocessor-Version-fat.jar -conf src/test/resources/config.json'
2. Run from inside IDEA creating a JAR Application build configuration with program arguments `-conf src/test/resources/config.json`


Try the following urls in your browser:
* `http://localhost:8080/test`


## The Configuration

An HK2 binder `ExampleBinder` is used to inject the `VertxRequestProcessor`s and `VertxResponseProcessor`s
