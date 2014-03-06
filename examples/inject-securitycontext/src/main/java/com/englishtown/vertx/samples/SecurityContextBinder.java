package com.englishtown.vertx.samples;

import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/9/13
 * Time: 7:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class SecurityContextBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(CustomSecurityContextProvider.class).to(VertxRequestProcessor.class).ranked(10);
    }
}
