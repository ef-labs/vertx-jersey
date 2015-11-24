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

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * {@link DefaultJerseyOptions} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJerseyOptionsTest {

    @Mock
    private Vertx vertx;
    @Mock
    private Context context;
    @Mock
    private Logger logger;

    private JsonObject config = new JsonObject();
    private DefaultJerseyOptions options;

    @Before
    public void setUp() throws Exception {

        when(vertx.getOrCreateContext()).thenReturn(context);
        when(context.config()).thenReturn(config);

        options = new DefaultJerseyOptions(vertx);

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
    public void testGetPackages() throws Exception {

        String package1 = "com.englishtown.vertx.jersey.resources1";
        String package2 = "com.englishtown.vertx.jersey.resources2";

        config.put(DefaultJerseyOptions.CONFIG_RESOURCES, new JsonArray().add(package1));
        config.put(DefaultJerseyOptions.CONFIG_PACKAGES, new JsonArray().add(package2));

        List<String> packages = options.getPackages();

        assertNotNull(packages);
        assertEquals(2, packages.size());
        assertEquals(package1, packages.get(0));
        assertEquals(package2, packages.get(1));

    }

    @Test
    public void testGetComponents() throws Exception {

        config.put(DefaultJerseyOptions.CONFIG_COMPONENTS, new JsonArray().add(MyObj.class.getName()));
        config.put(DefaultJerseyOptions.CONFIG_FEATURES, new JsonArray().add(MyBinder.class.getName()));

        Set<Class<?>> instances = options.getComponents();

        assertNotNull(instances);
        assertEquals(2, instances.size());
        assertTrue(instances.contains(MyObj.class));
        assertTrue(instances.contains(MyBinder.class));

    }

    @Test(expected = RuntimeException.class)
    public void testGetComponents_Fail() throws Exception {

        config.put(DefaultJerseyOptions.CONFIG_COMPONENTS, new JsonArray().add(MyObj.class.getName() + ".invalid"));
        options.getComponents();
        fail();

    }

    @Test
    public void testGetInstances() throws Exception {

        config.put(DefaultJerseyOptions.CONFIG_INSTANCES, new JsonArray().add(MyObj.class.getName()));
        config.put(DefaultJerseyOptions.CONFIG_BINDERS, new JsonArray().add(MyBinder.class.getName()));

        Set<Object> instances = options.getInstances();

        assertNotNull(instances);
        assertEquals(2, instances.size());

        List<Object> list = new ArrayList<>();
        list.addAll(instances);
        assertThat(list.get(0), instanceOf(MyObj.class));
        assertThat(list.get(1), instanceOf(MyBinder.class));

    }

    @Test(expected = RuntimeException.class)
    public void testGetInstances_Fail() throws Exception {

        config.put(DefaultJerseyOptions.CONFIG_INSTANCES, new JsonArray().add(MyObj.class.getName() + ".invalid"));
        options.getInstances();
        fail();

    }

    @Test
    public void testGetProperties() throws Exception {

        JsonObject rc = new JsonObject().put("prop1", "a");
        config.put(DefaultJerseyOptions.CONFIG_RESOURCE_CONFIG, rc);
        assertEquals(rc.getMap(), options.getProperties());

    }

    public static class MyObj {
        @Override
        public int hashCode() {
            return 1;
        }
    }

    public static class MyBinder extends AbstractBinder {
        @Override
        protected void configure() {
        }

        @Override
        public int hashCode() {
            return 2;
        }
    }

}
