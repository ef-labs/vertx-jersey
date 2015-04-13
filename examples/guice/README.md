# Guice DI with vertx-jersey

This is an example of using guice with the hk2 bridge for DI.  The following dependencies must be on the class path:

```xml
        <dependency>
            <groupId>com.google.inject.extensions</groupId>
            <artifactId>guice-multibindings</artifactId>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.glassfish.hk2</groupId>
            <artifactId>guice-bridge</artifactId>
            <scope>compile</scope>
        </dependency>
```

Guice's Multibindings extension is used to inject sets of Jersey filters, request processors, model processors, etc.


## Run It

1. Run from the command line `java -jar target/vertx-jersey-examples-guice-Version-fat.jar -conf src/test/resources/config.json'
2. Run from inside IDEA creating a JAR Application build configuration with program arguments `-conf src/test/resources/config.json`

Try the following url in your browser:
* `http://localhost:8080/guice`


## Configuration

See the guice module `com.englishtown.vertx.jersey.examples.guice.CustomBinder`.  The `GuiceJerseyBinder` is installed and 6 custom bindings are created.
