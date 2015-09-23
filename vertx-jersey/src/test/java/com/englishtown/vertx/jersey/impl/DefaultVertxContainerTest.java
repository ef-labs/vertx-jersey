package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.JerseyOptions;
import io.vertx.core.Vertx;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultVertxContainer}
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultVertxContainerTest {

    private DefaultVertxContainer container;
    private ServiceLocator locator;
    private List<String> packages = new ArrayList<>();

    @Mock
    Vertx vertx;
    @Mock
    JerseyOptions options;

    @Before
    public void setUp() throws Exception {

        when(options.getPackages()).thenReturn(packages);

        locator = ServiceLocatorFactory.getInstance().create(null);
        container = new DefaultVertxContainer(vertx, locator);
    }

    @Test
    public void testInit() throws Exception {

        packages.add("com.englishtown.vertx.jersey.resources");

        assertNull(container.getOptions());
        assertNull(container.getConfiguration());
        assertNull(container.getApplicationHandler());
        assertNull(container.getApplicationHandlerDelegate());

        container.init(options);

        assertEquals(options, container.getOptions());
        assertNotNull(container.getConfiguration());
        assertNotNull(container.getApplicationHandler());
        assertNotNull(container.getApplicationHandlerDelegate());

    }

    @Test
    public void testInit_Missing_Resources() throws Exception {

        try {
            container.init(options);
            fail();

        } catch (IllegalStateException e) {
            assertEquals("At least one resource package name must be specified", e.getMessage());

        }

    }

}