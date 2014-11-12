/*
 * The MIT License (MIT)
 * Copyright © 2013 Englishtown <opensource@englishtown.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.englishtown.vertx.jersey.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

import java.net.URI;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.internal.ServiceLocatorImpl;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;

/**
 * {@link DefaultJerseyConfigurator} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJerseyConfiguratorTest {

    @Mock
    Vertx vertx;
    @Mock
    Logger logger;

    JsonObject config;
    DefaultJerseyConfigurator configurator;

    @Before
    public void setUp() throws Exception {

        config = new JsonObject();

        ServiceLocator locator = new ServiceLocatorImpl("test", null);
        configurator = new DefaultJerseyConfigurator(locator);
        configurator.init(config, vertx);

    }

    @Test
    public void testInit_No_Config() throws Exception {

        DefaultJerseyConfigurator configurator = new DefaultJerseyConfigurator(null);

        try {
            configurator.init(null, null);
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
        config.put(DefaultJerseyConfigurator.CONFIG_BASE_PATH, expected);
        expected += "/";

        uri = configurator.getBaseUri();
        assertEquals(expected, uri.getPath());

    }

    @Test
    public void testGetApplicationHandler() throws Exception {

        JsonArray resources = new JsonArray().add("com.englishtown.vertx.resources");
        config.put(DefaultJerseyConfigurator.CONFIG_RESOURCES, resources);

        ApplicationHandlerDelegate applicationHandlerDelegate;
        applicationHandlerDelegate = configurator.getApplicationHandler();

        assertNotNull(applicationHandlerDelegate);

    }

    @Test
    public void testGetMaxBodySize() throws Exception {

        int expected = DefaultJerseyConfigurator.DEFAULT_MAX_BODY_SIZE;
        int maxBodySize;

        maxBodySize = configurator.getMaxBodySize();
        assertEquals(expected, maxBodySize);

        expected = 12;
        config.put(DefaultJerseyConfigurator.CONFIG_MAX_BODY_SIZE, expected);

        maxBodySize = configurator.getMaxBodySize();
        assertEquals(expected, maxBodySize);

    }

    @Test
    public void testGetResourceConfig_Missing_Resources() throws Exception {

        try {
            configurator.getResourceConfig();
            fail();

        } catch (RuntimeException e) {
            assertEquals("At least one resource package name must be specified in the config " +
                    "resources", e.getMessage());

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

        JsonArray resources = new JsonArray().add("com.englishtown.vertx.jersey.resources");
        config.put(DefaultJerseyConfigurator.CONFIG_RESOURCES, resources);
        resourceConfig = configurator.getResourceConfig();

        assertNotNull(resourceConfig);
        assertEquals(1, resourceConfig.getClasses().size());
        assertEquals(1, resourceConfig.getInstances().size());

        JsonArray features = new JsonArray().add("com.englishtown.vertx.jersey.inject.TestFeature");
        config.put(DefaultJerseyConfigurator.CONFIG_FEATURES, features);
        resourceConfig = configurator.getResourceConfig();

        assertNotNull(resourceConfig);
        assertEquals(2, resourceConfig.getClasses().size());

        JsonArray binders = new JsonArray().add("com.englishtown.vertx.jersey.inject.TestBinder2");
        config.put(DefaultJerseyConfigurator.CONFIG_BINDERS, binders);
        resourceConfig = configurator.getResourceConfig();

        assertNotNull(resourceConfig);
        assertEquals(2, resourceConfig.getClasses().size());
        assertEquals(2, resourceConfig.getInstances().size());

        binders.add("com.englishtown.vertx.jersey.inject.ClassNotFoundBinder");
        try {
            configurator.getResourceConfig();
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

        features.add("com.englishtown.vertx.jersey.inject.ClassNotFoundFeature");
        try {
            configurator.getResourceConfig();
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

    }
}
