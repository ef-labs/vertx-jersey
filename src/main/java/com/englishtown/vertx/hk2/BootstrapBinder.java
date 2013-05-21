package com.englishtown.vertx.hk2;

import com.englishtown.vertx.jersey.inject.VertxJerseyBinder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Default HK2 bootstrap binder used by the HK2VerticleFactory.
 * <p/>
 * Replace with your own bootstrap to configure {@link
 * com.englishtown.vertx.jersey.inject.VertxRequestProcessor} and
 * {@link com.englishtown.vertx.jersey.inject.VertxResponseProcessor} classes.
 */
public class BootstrapBinder extends AbstractBinder {
    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {
        install(new VertxJerseyBinder());
    }
}
