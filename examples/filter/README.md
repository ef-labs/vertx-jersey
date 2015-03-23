# filter

This sample shows injecting a jersey filter

## Run It

1. Run from the command line `java -jar target/vertx-jersey-examples-filter-Version-fat.jar -conf src/test/resources/config.json'
2. Run from inside IDEA creating a JAR Application build configuration with program arguments `-conf src/test/resources/config.json`


Try the following url in your browser:
* `http://localhost:8080/test`


## Configuration

The `FilterBinder` is used to register the container response filter.