package com.englishtown.vertx.guice;

import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.impl.DefaultJerseyServer;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Guice extension of {@link com.englishtown.vertx.jersey.impl.DefaultJerseyServer}
 */
public class GuiceJerseyServer extends DefaultJerseyServer {

    @Inject
    public GuiceJerseyServer(Provider<JerseyHandler> jerseyHandlerProvider, ServiceLocator locator, Injector injector) {
        super(jerseyHandlerProvider);
        initBridge(locator, injector);
    }

    private void initBridge(ServiceLocator locator, Injector injector) {
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(locator);
        GuiceIntoHK2Bridge guiceBridge = locator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(injector);
        injectMultiBindings(locator, injector);
    }

    /**
     * This is a workaround for the hk2 bridge limitations
     *
     * @param locator the HK2 locator
     * @param injector the Guice injector
     */
    private void injectMultiBindings(ServiceLocator locator, Injector injector) {

        injectMultiBindings(locator, injector, new Key<Set<ContainerRequestFilter>>() {
        }, ContainerRequestFilter.class);
        injectMultiBindings(locator, injector, new Key<Set<ContainerResponseFilter>>() {
        }, ContainerResponseFilter.class);
        injectMultiBindings(locator, injector, new Key<Set<ReaderInterceptor>>() {
        }, ReaderInterceptor.class);
        injectMultiBindings(locator, injector, new Key<Set<WriterInterceptor>>() {
        }, WriterInterceptor.class);
        injectMultiBindings(locator, injector, new Key<Set<ModelProcessor>>() {
        }, ModelProcessor.class);

    }

    private void injectMultiBindings(ServiceLocator locator, Injector injector, Key<? extends Set<?>> key, Type type) {

        Set<?> set = injector.getInstance(key);

        if (set != null && !set.isEmpty()) {
            for (Object obj : set) {
                ServiceLocatorUtilities.addOneConstant(locator, obj, null, type);
            }
        }

    }

}
