package com.englishtown.vertx.jersey.integration;

import com.englishtown.promises.Promise;
import com.englishtown.vertx.guice.GuiceVertxBinder;
import com.englishtown.vertx.guice.WhenGuiceJerseyBinder;
import com.englishtown.vertx.jersey.JerseyVerticle;
import com.englishtown.vertx.promises.guice.GuiceWhenBinder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.Vertx;

import java.util.Arrays;
import java.util.List;

/**
 * Guice implementation of {@link TestServiceLocator}
 */
public class GuiceTestServiceLocator implements TestServiceLocator {

    protected Injector injector;

    @Override
    public void init(Vertx vertx) {
        injector = Guice.createInjector(getModules(vertx));
    }

    protected List<Module> getModules(Vertx vertx) {
        return Arrays.asList(
                new GuiceVertxBinder(vertx),
                new WhenGuiceJerseyBinder(),
                new GuiceWhenBinder());
    }

    @Override
    public void tearDown() {
        injector = null;
    }

    @Override
    public <T> T getService(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    public Promise<String> deployJerseyVerticle() {
        return deployJerseyVerticle("java-guice:" + JerseyVerticle.class.getName());
    }
}
