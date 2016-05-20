package com.englishtown.vertx.jersey.integration;

import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.impl.DefaultJerseyOptions;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.junit.Test;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Integration tests for container lifecycle events
 */
public abstract class ContainerLifecycleListenerIntegrationTests extends VertxTestBase {

    private final ListenerServiceLocator locator;
    private TestLifeCycleListener listener;

    protected ContainerLifecycleListenerIntegrationTests(ListenerServiceLocator locator) {
        this.locator = locator;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        locator.init(vertx);
        listener = locator.getListener();

    }

    @Test
    public void testListener() throws Exception {

        vertx.runOnContext(aVoid -> {

            vertx.getOrCreateContext()
                    .config()
                    .put("jersey", new JsonObject()
                            .put(DefaultJerseyOptions.CONFIG_PORT, 8080)
                            .put(DefaultJerseyOptions.CONFIG_PACKAGES, new JsonArray()
                                    .add("com.englishtown.vertx.jersey.resources")));

            assertFalse(listener.started);
            assertFalse(listener.shutdown);

            JerseyServer server = locator.getService(JerseyServer.class);

            assertFalse(listener.started);
            assertFalse(listener.shutdown);

            server.start(result -> {
                assertTrue(result.succeeded());

                assertTrue(listener.started);
                assertFalse(listener.shutdown);

                server.close();
                assertTrue(listener.shutdown);

                testComplete();

            });
        });

        await();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        locator.tearDown();
    }

    public static class TestLifeCycleListener implements ContainerLifecycleListener {

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

    public interface ListenerServiceLocator extends TestServiceLocator {

        TestLifeCycleListener getListener();

    }

    public static class Guice extends ContainerLifecycleListenerIntegrationTests {

        public Guice() {
            super(new GuiceListenerLocator());
        }

        private static class GuiceListenerLocator extends GuiceTestServiceLocator implements ListenerServiceLocator {

            @Override
            protected List<Module> getModules(Vertx vertx) {
                List<Module> modules = new ArrayList<>(super.getModules(vertx));
                modules.add(new AbstractModule() {
                    @Override
                    protected void configure() {
                        Multibinder.newSetBinder(binder(), ContainerLifecycleListener.class).addBinding().to(TestLifeCycleListener.class).in(Singleton.class);
                    }
                });
                return modules;
            }

            @Override
            public TestLifeCycleListener getListener() {
                Set<ContainerLifecycleListener> set = injector.getInstance(new Key<Set<ContainerLifecycleListener>>() {
                });
                return (TestLifeCycleListener) set.iterator().next();
            }
        }

    }

    public static class HK2 extends ContainerLifecycleListenerIntegrationTests {

        public HK2() {
            super(new HK2ListenerLocator());
        }

        private static class HK2ListenerLocator extends HK2TestServiceLocator implements ListenerServiceLocator {

            @Override
            public void init(Vertx vertx) {
                super.init(vertx);

                ServiceLocatorUtilities.bind(locator, new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(TestLifeCycleListener.class).to(ContainerLifecycleListener.class).in(Singleton.class);
                    }
                });
            }

            @Override
            public TestLifeCycleListener getListener() {
                List<ContainerLifecycleListener> listeners = locator.getAllServices(ContainerLifecycleListener.class);
                return (TestLifeCycleListener) listeners.get(0);
            }
        }

    }
}
