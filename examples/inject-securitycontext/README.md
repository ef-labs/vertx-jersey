# inject-securitycontext

This sample shows running jersey inside vert.x using a SecurityContextProvider to provide a custom SecurityContext.
It also demonstrates using the JAX-RS `@RolesAllowed` security annotation.


## Run It

1. Run from the command line `java -jar target/vertx-jersey-examples-inject-securitycontext-Version-fat.jar -conf src/test/resources/config.json'
2. Run from inside IDEA creating a JAR Application build configuration with program arguments `-conf src/test/resources/config.json`

Try the following urls in your browser:
* `http://localhost:8080/test`
* `http://localhost:8080/test/secure` - 403 Forbidden
* `http://localhost:8080/test/secure?role=1` - 200 OK
* `http://localhost:8080/test/secure?role=3` - 403 Forbidden


## The Configuration

The `config.json` file adds the HK2 `SecurityContextBinder` and the feature `org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature'

