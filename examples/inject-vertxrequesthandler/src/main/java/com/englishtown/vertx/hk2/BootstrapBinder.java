package com.englishtown.vertx.hk2;

import com.englishtown.vertx.jersey.inject.VertxJerseyBinder;
import com.englishtown.vertx.samples.ExampleBinder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/24/13
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class BootstrapBinder extends AbstractBinder {
    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {
        install(new VertxJerseyBinder(), new ExampleBinder());
    }
}
