package com.englishtown.vertx.samples;

import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/9/13
 * Time: 7:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExampleBinder extends AbstractBinder {
    @Override
    protected void configure() {
        // Request processors
        bind(ExampleVertxRequestProcessor.class).to(VertxRequestProcessor.class);
        bind(OtherVertxRequestProcessor.class).to(VertxRequestProcessor.class);
        // Response processors
        bind(ExampleVertxResponseProcessor.class).to(VertxResponseProcessor.class);
        bind(OtherVertxResponseProcessor.class).to(VertxResponseProcessor.class);
    }
}
