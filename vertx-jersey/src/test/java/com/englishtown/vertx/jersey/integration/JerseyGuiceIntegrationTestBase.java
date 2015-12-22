package com.englishtown.vertx.jersey.integration;

/**
 * Base class for jersey integration tests
 */
public abstract class JerseyGuiceIntegrationTestBase extends JerseyIntegrationTestBase {

    protected JerseyGuiceIntegrationTestBase() {
        super(new GuiceTestServiceLocator());
    }

}
