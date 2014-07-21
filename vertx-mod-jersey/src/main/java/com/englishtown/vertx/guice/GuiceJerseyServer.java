package com.englishtown.vertx.guice;

import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.impl.DefaultJerseyServer;
import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;
import javax.inject.Provider;

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
    }

}
