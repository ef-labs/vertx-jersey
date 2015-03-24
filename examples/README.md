
# Vert.x Jersey Example Modules

Build fat jars using the maven `fat-jar` profile.  Run the following from the vertx-jersey-parent directory:

```
mvn clean package -P fat-jar
```

Each example can then be run from its directory with the following:

```
java -jar target/{artifactId}-{version}-fat.jar -conf src/test/resources/config.json
```

See the README files for details and test URLs

* [chunked](chunked/README.md) - return chunked and streamed responses
* [filter](filter/README.md) - add a JAX-RS `ContainerResponseFilter` filter
* [guice](guice/README.md) - use vertx-guice for DI
* [inject-securitycontext](inject-securitycontext/README.md) - inject a custom `SecurityContext` and use role based filtering.
* [inject-vertx](inject-vertx/README.md) - inject standard vert.x objects
* [inject-vertxrequestprocessor](inject-vertxrequestprocessor/README.md) - inject `VertxRequestProcessor` and `VertxResponseProcessor` instances.
* [jackson](jackson/README.md) - use Jackson for JSON serialization and deserialization
* [maven-service](maven-service) - use the vertx-maven-service-factory
* [when.java](when.java/README.md) - use vertx-when to start a JerseyServer instance from a verticle.
