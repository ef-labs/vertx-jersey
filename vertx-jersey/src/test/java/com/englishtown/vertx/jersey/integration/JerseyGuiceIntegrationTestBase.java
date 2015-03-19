package com.englishtown.vertx.jersey.integration;

import com.englishtown.promises.Promise;
import com.englishtown.vertx.guice.GuiceVertxBinder;
import com.englishtown.vertx.guice.WhenGuiceJerseyBinder;
import com.englishtown.vertx.hk2.HK2VertxBinder;
import com.englishtown.vertx.hk2.WhenHK2JerseyBinder;
import com.englishtown.vertx.jersey.JerseyVerticle;
import com.englishtown.vertx.promises.guice.GuiceWhenBinder;
import com.englishtown.vertx.promises.hk2.HK2WhenBinder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * Base class for jersey integration tests
 */
public abstract class JerseyGuiceIntegrationTestBase extends JerseyIntegrationTestBase {

    protected Injector injector;

    @Override
    protected <T> T getService(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    protected void init() throws Exception {
        injector = Guice.createInjector(new GuiceVertxBinder(vertx), new WhenGuiceJerseyBinder(), new GuiceWhenBinder());
        super.init();
    }

    @Override
    protected Promise<String> deployJerseyVerticle() {
        return deployJerseyVerticle("java-guice:" + JerseyVerticle.class.getName());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (injector != null) {
            injector = null;
        }
    }

}
