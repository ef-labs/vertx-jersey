package com.englishtown.vertx.guice;

import com.google.inject.AbstractModule;

/**
 * Guice bootstrap jersey binder
 */
public class BootstrapBinder extends AbstractModule {

    /**
     * Configures a {@link com.google.inject.Binder} via the exposed methods.
     */
    @Override
    protected void configure() {
        install(new GuiceJerseyBinder());
    }

}
