# vertx-mod-whenjersey

Provides when.java wrappers for vertx-mod-jersey objects

See also:
* https://github.com/englishtown/vertx-mod-when
* https://github.com/englishtown/vertx-mod-jersey

# Example

The following example assumes a `com.englishtown.vertx.jersey.promises.WhenJerseyServer` instance has been injected using the `com.englishtown.vertx.hk2.WhenJerseyBinder` and vertx-mod-hk2 module.

```java

    @Override
    public void start(final Future<Void> startedResult) {

        // Get the jersey server configuration
        JsonObject config = config.getObject("jersey"); new JsonObject()

        // Create the jersey server and set the startedResult
        // NOTE: there is also an overload that takes a Handler<ResourceConfig> call back to provide additional
        //       initialization to the ResourceConfig object before the Jersey ApplicationHandler is created.
        whenJerseyServer.createServer(config).then(
                new FulfilledRunnable<JerseyServer>() {
                    @Override
                    public Promise<JerseyServer, Void> run(JerseyServer value) {
                        start();
                        startedResult.setResult(null);
                        return null;
                    }
                },
                new RejectedRunnable<JerseyServer>() {
                    @Override
                    public Promise<JerseyServer, Void> run(Value<JerseyServer> value) {
                        startedResult.setFailure(value.error);
                        return null;
                    }
                }
        );

    }

```
