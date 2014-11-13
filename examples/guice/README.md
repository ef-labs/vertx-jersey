# Guice DI with ext-jersey

This is an example of using guice with the hk2 bridge for DI.  The following dependencies must be on the class path:

```xml
        <dependency>
            <groupId>org.glassfish.hk2</groupId>
            <artifactId>hk2-locator</artifactId>
            <scope>compile</scope>
        </dependency>
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

See the guice module `com.englishtown.vertx.jersey.examples.guice.CustomBinder`.  The `GuiceJerseyBinder` is installed and 6 custom bindings are created.
