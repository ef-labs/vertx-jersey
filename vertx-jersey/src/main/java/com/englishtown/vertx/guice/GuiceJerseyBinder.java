package com.englishtown.vertx.guice;

import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.impl.DefaultJerseyHandler;
import com.englishtown.vertx.jersey.impl.DefaultJerseyOptions;
import com.englishtown.vertx.jersey.impl.WriteStreamBodyWriter;
import com.englishtown.vertx.jersey.inject.ContainerResponseWriterProvider;
import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import com.englishtown.vertx.jersey.inject.impl.VertxResponseWriterProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.jersey.server.model.ModelProcessor;

import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Guice Jersey binder
 */
public class GuiceJerseyBinder extends AbstractModule {

    /**
     * Configures a {@link com.google.inject.Binder} via the exposed methods.
     */
    @Override
    protected void configure() {

        // Create HK2 service locator and bind jersey injections
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null);
        bind(ServiceLocator.class).toInstance(locator);

        bind(JerseyServer.class).to(GuiceJerseyServer.class);
        bind(JerseyHandler.class).to(DefaultJerseyHandler.class);
        bind(JerseyOptions.class).to(DefaultJerseyOptions.class);
        bind(ContainerResponseWriterProvider.class).to(VertxResponseWriterProvider.class);
        bind(MessageBodyWriter.class).to(WriteStreamBodyWriter.class).in(Singleton.class);

        Multibinder.newSetBinder(binder(), VertxRequestProcessor.class);
        Multibinder.newSetBinder(binder(), VertxResponseProcessor.class);
        Multibinder.newSetBinder(binder(), VertxPostResponseProcessor.class);

        Multibinder.newSetBinder(binder(), ContainerRequestFilter.class);
        Multibinder.newSetBinder(binder(), ContainerResponseFilter.class);
        Multibinder.newSetBinder(binder(), ReaderInterceptor.class);
        Multibinder.newSetBinder(binder(), WriterInterceptor.class);
        Multibinder.newSetBinder(binder(), ModelProcessor.class);

    }

    @Provides
    List<VertxRequestProcessor> provideVertxRequestProcessorList(Set<VertxRequestProcessor> processors) {
        return new ArrayList<>(processors);
    }

    @Provides
    List<VertxResponseProcessor> provideVertxResponseProcessorList(Set<VertxResponseProcessor> processors) {
        return new ArrayList<>(processors);
    }

    @Provides
    List<VertxPostResponseProcessor> provideVertxPostResponseProcessorList(Set<VertxPostResponseProcessor> processors) {
        return new ArrayList<>(processors);
    }

}
