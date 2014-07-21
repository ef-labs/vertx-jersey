package com.englishtown.vertx.jersey.examples.guice;

import com.englishtown.vertx.guice.GuiceJerseyBinder;
import com.englishtown.vertx.jersey.examples.guice.impl.DefaultMyDependency;
import com.englishtown.vertx.jersey.examples.guice.inject.*;
import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Custom guice binder
 */
public class CustomBinder extends AbstractModule {
    /**
     * Configures a {@link com.google.inject.Binder} via the exposed methods.
     */
    @Override
    protected void configure() {

        install(new GuiceJerseyBinder());

        // POJOs
        bind(MyDependency.class).to(DefaultMyDependency.class);

        // vertx-mod-jersey interfaces
        Multibinder.newSetBinder(binder(), VertxRequestProcessor.class).addBinding().to(GuiceRequestProcessor.class);
        Multibinder.newSetBinder(binder(), VertxResponseProcessor.class).addBinding().to(GuiceResponseProcessor.class);
        Multibinder.newSetBinder(binder(), VertxPostResponseProcessor.class).addBinding().to(GuicePostResponseProcessor.class);

        // Jersey interfaces
        Multibinder.newSetBinder(binder(), ContainerRequestFilter.class).addBinding().to(GuiceRequestFilter.class);
        Multibinder.newSetBinder(binder(), ContainerResponseFilter.class).addBinding().to(GuiceResponseFilter.class);

    }

    @Provides
    List<ContainerRequestFilter> provideContainerRequestFilters(Set<ContainerRequestFilter> filters) {
        return new ArrayList<>(filters);
    }

    @Provides
    List<ContainerResponseFilter> provideContainerResponseFilters(Set<ContainerResponseFilter> filters) {
        return new ArrayList<>(filters);
    }

}
