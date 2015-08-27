package com.englishtown.vertx.jersey.integration;

import com.englishtown.vertx.hk2.HK2JerseyBinder;
import com.englishtown.vertx.hk2.HK2VertxBinder;
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.impl.DefaultJerseyOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.junit.Test;

/**
 * Integration tests for container lifecycle events
 */
public class ContainerLifecycleListenerIntegrationTests extends VertxTestBase {

    private ServiceLocator locator;
    private TestLifeCycleListener listener = new TestLifeCycleListener();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        locator = ServiceLocatorUtilities.bind(
                new HK2JerseyBinder(),
                new HK2VertxBinder(vertx),
                new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(listener).to(ContainerLifecycleListener.class);
                    }
                });

    }

    @Test
    public void testListener() throws Exception {

        assertFalse(listener.started);
        assertFalse(listener.shutdown);

        JerseyServer server = locator.getService(JerseyServer.class);
        JerseyOptions options = locator.getService(JerseyOptions.class);

        JsonObject config = new JsonObject()
                .put(DefaultJerseyOptions.CONFIG_PACKAGES, new JsonArray()
                        .add("com.englishtown.vertx.jersey.resources"));

        options.init(config);

        assertFalse(listener.started);
        assertFalse(listener.shutdown);

        server.init(options);

        assertTrue(listener.started);
        assertFalse(listener.shutdown);

        server.close();
        assertTrue(listener.shutdown);

    }

    private static class TestLifeCycleListener implements ContainerLifecycleListener {

        private boolean started;
        private boolean shutdown;

        /**
         * Invoked at the {@link Container container} start-up. This method is invoked even
         * when application is reloaded and new instance of application has started.
         *
         * @param container container that has been started.
         */
        @Override
        public void onStartup(Container container) {
            started = true;
        }

        /**
         * Invoked when the {@link Container container} has been reloaded.
         *
         * @param container container that has been reloaded.
         */
        @Override
        public void onReload(Container container) {

        }

        /**
         * Invoke at the {@link Container container} shut-down. This method is invoked even before
         * the application is being stopped as a part of reload.
         *
         * @param container container that has been shut down.
         */
        @Override
        public void onShutdown(Container container) {
            shutdown = true;
        }
    }
}
