package com.englishtown.vertx.jersey.integration;

/**
 * Base class for jersey integration tests
 */
public abstract class JerseyHK2IntegrationTestBase extends JerseyIntegrationTestBase {

    public JerseyHK2IntegrationTestBase() {
        super(new HK2TestServiceLocator());
    }

}
