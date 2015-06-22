package com.englishtown.vertx.jersey.integration;

import com.englishtown.promises.Promise;
import com.englishtown.vertx.hk2.HK2VertxBinder;
import com.englishtown.vertx.hk2.WhenHK2JerseyBinder;
import com.englishtown.vertx.jersey.JerseyVerticle;
import com.englishtown.vertx.promises.hk2.HK2WhenBinder;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * Base class for jersey integration tests
 */
public abstract class JerseyHK2IntegrationTestBase extends JerseyIntegrationTestBase {

    protected ServiceLocator locator;

    @Override
    protected <T> T getService(Class<T> clazz) {
        return locator.getService(clazz);
    }

    @Override
    protected void init() throws Exception {
        locator = ServiceLocatorFactory.getInstance().create(null);
        ServiceLocatorUtilities.bind(locator, new HK2VertxBinder(vertx), new WhenHK2JerseyBinder(), new HK2WhenBinder());
        super.init();
    }

    @Override
    protected Promise<String> deployJerseyVerticle() {
        return deployJerseyVerticle("java-hk2:" + JerseyVerticle.class.getName());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (locator != null) {
            locator.shutdown();
            locator = null;
        }
    }

}
