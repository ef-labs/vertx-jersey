package com.englishtown.vertx.jersey.integration;

import com.englishtown.promises.Promise;
import com.englishtown.vertx.hk2.HK2VertxBinder;
import com.englishtown.vertx.hk2.WhenHK2JerseyBinder;
import com.englishtown.vertx.jersey.JerseyVerticle;
import com.englishtown.vertx.promises.hk2.HK2WhenBinder;
import io.vertx.core.Vertx;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * HK2 implementation of {@link TestServiceLocator}
 */
public class HK2TestServiceLocator implements TestServiceLocator {

    protected ServiceLocator locator;

    @Override
    public void init(Vertx vertx) {

        locator = ServiceLocatorUtilities.bind(
                new HK2VertxBinder(vertx),
                new WhenHK2JerseyBinder(),
                new HK2WhenBinder());

    }

    @Override
    public void tearDown() {
        if (locator != null) {
            locator.shutdown();
            locator = null;
        }
    }

    @Override
    public <T> T getService(Class<T> clazz) {
        return locator.getService(clazz);
    }

    @Override
    public Promise<String> deployJerseyVerticle() {
        return deployJerseyVerticle("java-hk2:" + JerseyVerticle.class.getName());
    }
}
