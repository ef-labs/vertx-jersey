package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.ApplicationConfigurator;
import com.englishtown.vertx.jersey.JerseyOptions;
import io.vertx.core.Vertx;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultVertxContainer}
 */
public class DefaultVertxContainerTest {

    private DefaultVertxContainer container;
    private ServiceLocator locator;
    private List<String> packages = new ArrayList<>();

    @Mock
    Vertx vertx;
    @Mock
    JerseyOptions options;

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        when(options.getPackages()).thenReturn(packages);
        locator = ServiceLocatorFactory.getInstance().create(null);
    }

    @Test
    public void testStart() throws Exception {

        packages.add("com.englishtown.vertx.jersey.resources");
        container = new DefaultVertxContainer(vertx, options, locator, null);
        container.start();

        assertEquals(options, container.getOptions());
        assertNotNull(container.getConfiguration());
        assertNotNull(container.getApplicationHandler());
        assertNotNull(container.getApplicationHandlerDelegate());

        // Can't mock applicationHandler so can't verify onStartup() was called

    }

    @Test
    public void testStop() throws Exception {

        packages.add("com.englishtown.vertx.jersey.resources");
        container = new DefaultVertxContainer(vertx, options, locator, null);
        container.start();

        container.stop();
        // Can't mock applicationHandler so can't verify onShutdown() was called

    }

    @Test
    public void testResourceConfigModifier() throws Exception {

        boolean[] b = {false};

        ApplicationConfigurator configurator = rc -> {
            b[0] = true;
            return rc;
        };

        packages.add("com.englishtown.vertx.jersey.resources");
        container = new DefaultVertxContainer(vertx, options, locator, configurator);
        container.start();

        assertTrue(b[0]);

    }

    @Test
    public void testInit_Missing_Resources() throws Exception {

        try {
            container = new DefaultVertxContainer(vertx, options, locator, null);
            container.start();
            fail();

        } catch (IllegalStateException e) {
            assertEquals("At least one resource package name or component must be specified", e.getMessage());

        }

    }

}
