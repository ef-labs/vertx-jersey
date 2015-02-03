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

import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.internal.ServiceLocatorImpl;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static org.junit.Assert.*;

/**
 * {@link DefaultJerseyOptions} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJerseyOptionsTest {

    @Mock
    Vertx vertx;
    @Mock
    Logger logger;

    JsonObject config;
    DefaultJerseyOptions options;

    @Before
    public void setUp() throws Exception {

        config = new JsonObject();

        ServiceLocator locator = new ServiceLocatorImpl("test", null);
        options = new DefaultJerseyOptions(locator);
        options.init(config, vertx);

    }

    @Test
    public void testInit_No_Config() throws Exception {

        DefaultJerseyOptions options = new DefaultJerseyOptions(null);

        try {
            options.init(null, null);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            options.getMaxBodySize();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }

    }

    @Test
    public void testGetBaseUri() throws Exception {

        URI uri;
        String expected = "/";

        uri = options.getBaseUri();
        assertEquals(expected, uri.getPath());

        expected = "test/base/path";
        config.put(DefaultJerseyOptions.CONFIG_BASE_PATH, expected);
        expected += "/";

        uri = options.getBaseUri();
        assertEquals(expected, uri.getPath());

    }

    @Test
    public void testGetApplicationHandler() throws Exception {

        JsonArray resources = new JsonArray().add("com.englishtown.vertx.resources");
        config.put(DefaultJerseyOptions.CONFIG_RESOURCES, resources);

        ApplicationHandlerDelegate applicationHandlerDelegate;
        applicationHandlerDelegate = options.getApplicationHandler();

        assertNotNull(applicationHandlerDelegate);

    }

    @Test
    public void testGetMaxBodySize() throws Exception {

        int expected = DefaultJerseyOptions.DEFAULT_MAX_BODY_SIZE;
        int maxBodySize;

        maxBodySize = options.getMaxBodySize();
        assertEquals(expected, maxBodySize);

        expected = 12;
        config.put(DefaultJerseyOptions.CONFIG_MAX_BODY_SIZE, expected);

        maxBodySize = options.getMaxBodySize();
        assertEquals(expected, maxBodySize);

    }

    @Test
    public void testGetResourceConfig_Missing_Resources() throws Exception {

        try {
            options.getResourceConfig();
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
            options.getResourceConfig();
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

        JsonArray resources = new JsonArray().add("com.englishtown.vertx.jersey.resources");
        config.put(DefaultJerseyOptions.CONFIG_RESOURCES, resources);
        resourceConfig = options.getResourceConfig();

        assertNotNull(resourceConfig);
        assertEquals(1, resourceConfig.getClasses().size());
        assertEquals(1, resourceConfig.getInstances().size());

        JsonArray features = new JsonArray().add("com.englishtown.vertx.jersey.inject.TestFeature");
        config.put(DefaultJerseyOptions.CONFIG_FEATURES, features);
        resourceConfig = options.getResourceConfig();

        assertNotNull(resourceConfig);
        assertEquals(2, resourceConfig.getClasses().size());

        JsonArray binders = new JsonArray().add("com.englishtown.vertx.jersey.inject.TestBinder2");
        config.put(DefaultJerseyOptions.CONFIG_BINDERS, binders);
        resourceConfig = options.getResourceConfig();

        assertNotNull(resourceConfig);
        assertEquals(2, resourceConfig.getClasses().size());
        assertEquals(2, resourceConfig.getInstances().size());

        binders.add("com.englishtown.vertx.jersey.inject.ClassNotFoundBinder");
        try {
            options.getResourceConfig();
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

        features.add("com.englishtown.vertx.jersey.inject.ClassNotFoundFeature");
        try {
            options.getResourceConfig();
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

    }
}
