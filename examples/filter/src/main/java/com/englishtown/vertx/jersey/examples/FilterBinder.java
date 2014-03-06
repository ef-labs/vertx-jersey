package com.englishtown.vertx.jersey.examples;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.ws.rs.container.ContainerResponseFilter;

/**
 * HK2 binder for injecting filters
 */
public class FilterBinder extends AbstractBinder {
    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {
        bind(PoweredByResponseFilter.class).to(ContainerResponseFilter.class);
    }
}
