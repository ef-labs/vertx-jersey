package com.englishtown.vertx.jersey.examples;

import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ExampleBinder extends AbstractBinder {
    @Override
    protected void configure() {
        // Request processors
        bind(ExampleVertxRequestProcessor.class).to(VertxRequestProcessor.class);
        bind(OtherVertxRequestProcessor.class).to(VertxRequestProcessor.class);
        // Response processors
        bind(ExampleVertxResponseProcessor.class).to(VertxResponseProcessor.class);
        bind(OtherVertxResponseProcessor.class).to(VertxResponseProcessor.class);
        // Post response processors
        bind(ExampleVertxPostResponseProcessor.class).to(VertxPostResponseProcessor.class);
    }
}
