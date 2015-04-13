# vertx-jersey-jackson

This sample shows running jersey inside vert.x using the `org.glassfish.jersey.jackson.JacksonFeature` for json
serialization.


## Run It

1. Run from the command line `java -jar target/vertx-jersey-examples-jackson-Version-fat.jar -conf src/test/resources/config.json'
2. Run from inside IDEA creating a JAR Application build configuration with program arguments `-conf src/test/resources/config.json`


Try the following urls in your browser:
* `http://localhost:8080/json`
* `http://localhost:8080/json/async`
* `http://localhost:8080/json/jsonp`
* `http://localhost:8080/json/jsonp?cb=fooback`


## The Configuration

Add the jersey feature `org.glassfish.jersey.jackson.JacksonFeature` to enable Jackson serialization.
