package com.englishtown.vertx.jersey.examples;

import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class SecurityContextBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(CustomSecurityContextProvider.class).to(VertxRequestProcessor.class).ranked(10);
    }
}
