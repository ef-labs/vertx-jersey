package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import java.net.URI;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link DefaultJerseyHandlerConfigurator} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJerseyHandlerConfiguratorTest {

    @Mock
    Vertx vertx;
    @Mock
    Container container;
    JsonObject config;
    DefaultJerseyHandlerConfigurator configurator;

    @Before
    public void setUp() throws Exception {

        config = new JsonObject();
        when(container.config()).thenReturn(config);

        configurator = new DefaultJerseyHandlerConfigurator();
        configurator.init(vertx, container);

    }

    @Test
    public void testInit_No_Config() throws Exception {

        DefaultJerseyHandlerConfigurator configurator = new DefaultJerseyHandlerConfigurator();

        try {
            configurator.init(mock(Vertx.class), mock(Container.class));
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            configurator.getMaxBodySize();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }

    }

    @Test
    public void testGetBaseUri() throws Exception {

        URI uri;
        String expected = "/";

        uri = configurator.getBaseUri();
        assertEquals(expected, uri.getPath());

        expected = "test/base/path";
        config.putString(DefaultJerseyHandlerConfigurator.CONFIG_BASE_PATH, expected);

        uri = configurator.getBaseUri();
        assertEquals(expected, uri.getPath());

    }

    @Test
    public void testGetApplicationHandler() throws Exception {

        JsonArray resources = new JsonArray().addString("com.englishtown.vertx.resources");
        config.putArray(DefaultJerseyHandlerConfigurator.CONFIG_RESOURCES, resources);

        ApplicationHandlerDelegate applicationHandlerDelegate;
        applicationHandlerDelegate = configurator.getApplicationHandler();

        assertNotNull(applicationHandlerDelegate);

    }

    @Test
    public void testGetMaxBodySize() throws Exception {

        int expected = DefaultJerseyHandlerConfigurator.DEFAULT_MAX_BODY_SIZE;
        int maxBodySize;

        maxBodySize = configurator.getMaxBodySize();
        assertEquals(expected, maxBodySize);

        expected = 12;
        config.putNumber(DefaultJerseyHandlerConfigurator.CONFIG_MAX_BODY_SIZE, expected);

        maxBodySize = configurator.getMaxBodySize();
        assertEquals(expected, maxBodySize);

    }

    @Test
    public void testGetResourceConfig_Missing_Resources() throws Exception {

        try {
            configurator.getResourceConfig();
            fail();

        } catch (RuntimeException e) {
            assertEquals(e.getMessage(), "At lease one resource package name must be specified in the config " +
                    "resources");

        }

    }

    @Test
    public void testGetResourceConfig() throws Exception {

        ResourceConfig resourceConfig;

        try {
            configurator.getResourceConfig();
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

        JsonArray resources = new JsonArray().addString("com.englishtown.vertx.jersey.resources");
        config.putArray(DefaultJerseyHandlerConfigurator.CONFIG_RESOURCES, resources);
        resourceConfig = configurator.getResourceConfig();

        assertNotNull(resourceConfig);
        assertEquals(1, resourceConfig.getClasses().size());
        assertEquals(0, resourceConfig.getInstances().size());

        JsonArray features = new JsonArray().addString("com.englishtown.vertx.jersey.inject.TestFeature");
        config.putArray(DefaultJerseyHandlerConfigurator.CONFIG_FEATURES, features);
        resourceConfig = configurator.getResourceConfig();

        assertNotNull(resourceConfig);
        assertEquals(2, resourceConfig.getClasses().size());

        JsonArray binders = new JsonArray().addString("com.englishtown.vertx.jersey.inject.TestBinder2");
        config.putArray(DefaultJerseyHandlerConfigurator.CONFIG_BINDERS, binders);
        resourceConfig = configurator.getResourceConfig();

        assertNotNull(resourceConfig);
        assertEquals(2, resourceConfig.getClasses().size());
        assertEquals(1, resourceConfig.getInstances().size());

        binders.addString("com.englishtown.vertx.jersey.inject.ClassNotFoundBinder");
        try {
            configurator.getResourceConfig();
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

        features.addString("com.englishtown.vertx.jersey.inject.ClassNotFoundFeature");
        try {
            configurator.getResourceConfig();
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

    }
}
