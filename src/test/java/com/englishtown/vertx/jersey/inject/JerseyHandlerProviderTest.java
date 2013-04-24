//package com.englishtown.vertx.jersey.inject;
//
//import com.englishtown.vertx.jersey.DefaultJerseyHandler;
//import org.glassfish.jersey.server.ResourceConfig;
//import org.junit.Test;
//import org.vertx.java.core.Vertx;
//import org.vertx.java.core.json.JsonArray;
//import org.vertx.java.core.json.JsonObject;
//import org.vertx.java.platform.Container;
//
//import java.net.URI;
//
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
///**
// * Created with IntelliJ IDEA.
// * User: adriangonzalez
// * Date: 4/23/13
// * Time: 12:09 PM
// * To change this template use File | Settings | File Templates.
// */
//public class JerseyHandlerProviderTest {
//
//    @Test
//    public void testGet() throws Exception {
//
//        JerseyHandlerProvider provider = createInstance();
//        DefaultJerseyHandler handler = provider.get();
//
//        assertNotNull(handler);
//
//    }
//
//    @Test
//    public void testGet_No_Config() throws Exception {
//
//        JerseyHandlerProvider provider = new JerseyHandlerProvider(mock(Vertx.class), mock(Container.class), null,
//                null);
//
//        try {
//            provider.get();
//            fail();
//        } catch (IllegalStateException e) {
//            // Expected
//        }
//
//    }
//
//    private JerseyHandlerProvider createInstance() {
//
//        Vertx vertx = mock(Vertx.class);
//        Container container = mock(Container.class);
//        JsonObject config = new JsonObject();
//        when(container.config()).thenReturn(config);
//
//        JsonArray resources = new JsonArray().addString("com.englishtown.vertx.jersey.resources");
//        config.putArray(JerseyHandlerProvider.CONFIG_RESOURCES, resources);
//
//        return new JerseyHandlerProvider(vertx, container, null, null);
//    }
//
//    @Test
//    public void testStart_Missing_Resources() throws Exception {
//
//        JerseyHandlerProvider provider = createInstance();
//
//        try {
//            provider.getResourceConfig(new JsonObject());
//            fail();
//
//        } catch (RuntimeException e) {
//            assertEquals(e.getMessage(), "At lease one resource package name must be specified in the config " +
//                    "resources");
//
//        }
//
//    }
//
//    @Test
//    public void testGetBaseUri() throws Exception {
//
//        JerseyHandlerProvider provider = createInstance();
//        JsonObject config = new JsonObject();
//        URI uri;
//        String expected = "/";
//
//        uri = provider.getBaseUri(config);
//        assertEquals(expected, uri.getPath());
//
//        expected = "test/base/path";
//        config.putString(JerseyHandlerProvider.CONFIG_BASE_PATH, expected);
//        uri = provider.getBaseUri(config);
//        assertEquals(expected, uri.getPath());
//
//    }
//
//    @Test
//    public void testGetResourceConfig() throws Exception {
//
//        JerseyHandlerProvider provider = createInstance();
//        JsonObject config = new JsonObject();
//        ResourceConfig resourceConfig;
//
//        try {
//            provider.getResourceConfig(config);
//            fail();
//        } catch (RuntimeException e) {
//            // Expected
//        }
//
//        JsonArray resources = new JsonArray().addString("com.englishtown.vertx.jersey.resources");
//        config.putArray(JerseyHandlerProvider.CONFIG_RESOURCES, resources);
//        resourceConfig = provider.getResourceConfig(config);
//
//        assertNotNull(resourceConfig);
//        assertEquals(1, resourceConfig.getClasses().size());
//        assertEquals(1, resourceConfig.getInstances().size());
//        assertTrue(resourceConfig.getInstances().iterator().next() instanceof VertxParamBinder);
//
//        JsonArray features = new JsonArray().addString("com.englishtown.vertx.jersey.inject.TestFeature");
//        config.putArray(JerseyHandlerProvider.CONFIG_FEATURES, features);
//        resourceConfig = provider.getResourceConfig(config);
//
//        assertNotNull(resourceConfig);
//        assertEquals(2, resourceConfig.getClasses().size());
//        assertEquals(1, resourceConfig.getInstances().size());
//
//        JsonArray binders = new JsonArray().addString("com.englishtown.vertx.jersey.inject.TestBinder2");
//        config.putArray(JerseyHandlerProvider.CONFIG_BINDERS, binders);
//        resourceConfig = provider.getResourceConfig(config);
//
//        assertNotNull(resourceConfig);
//        assertEquals(2, resourceConfig.getClasses().size());
//        assertEquals(2, resourceConfig.getInstances().size());
//
//        binders.addString("com.englishtown.vertx.jersey.inject.ClassNotFoundBinder");
//        try {
//            provider.getResourceConfig(config);
//            fail();
//        } catch (RuntimeException e) {
//            // Expected
//        }
//
//        features.addString("com.englishtown.vertx.jersey.inject.ClassNotFoundFeature");
//        try {
//            provider.getResourceConfig(config);
//            fail();
//        } catch (RuntimeException e) {
//            // Expected
//        }
//
//    }
//
//    @Test
//    public void testGetMaxBodySize() throws Exception {
//
//    }
//}
