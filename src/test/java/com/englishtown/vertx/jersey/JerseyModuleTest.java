package com.englishtown.vertx.jersey;

import org.junit.Test;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/18/13
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class JerseyModuleTest {
    @Test
    public void testStart_Missing_Resources() throws Exception {

        JerseyModule module = createInstance(new JsonObject());
        Future<Void> startedResult = mock(Future.class);

        try {
            module.start(startedResult);
            fail();

        } catch (RuntimeException e) {
            assertEquals(e.getMessage(), "At lease one resource package name must be specified in the config " +
                    "resources");

        }

    }

    @Test
    public void testStart() throws Exception {

        JsonObject config = new JsonObject()
                .putArray(JerseyModule.CONFIG_RESOURCES, new JsonArray().addString("com.englishtown.vertx.resources"));

        JerseyModule module = createInstance(config);
        Vertx vertx = module.getVertx();
        Future<Void> startedResult = mock(Future.class);

        module.start(startedResult);

        verify(vertx, times(1)).createHttpServer();
        HttpServer server = vertx.createHttpServer();
        verify(server, times(1)).listen(eq(80), eq("0.0.0.0"), any(Handler.class));

    }

    @Test
    public void testStart_Config() throws Exception {

        String host = "test.englishtown.com";
        int port = 8888;
        int bufferSize = 1024;
        String basePath = "test";

        JsonObject config = new JsonObject()
                .putString(JerseyModule.CONFIG_HOST, host)
                .putNumber(JerseyModule.CONFIG_PORT, port)
                .putNumber(JerseyModule.CONFIG_RECEIVE_BUFFER_SIZE, bufferSize)
                .putString(JerseyModule.CONFIG_BASE_PATH, basePath)
                .putArray(JerseyModule.CONFIG_BINDERS, new JsonArray().addString("com.englishtown.vertx.jersey.inject.TestBinder"))
                .putArray(JerseyModule.CONFIG_FEATURES, new JsonArray().addString("com.englishtown.vertx.jersey.inject.TestFeature"))
                .putArray(JerseyModule.CONFIG_RESOURCES, new JsonArray().addString("com.englishtown.vertx.resources"));

        JerseyModule module = createInstance(config);
        Vertx vertx = module.getVertx();
        Future<Void> startedResult = mock(Future.class);

        module.start(startedResult);

        verify(vertx, times(1)).createHttpServer();
        HttpServer server = vertx.createHttpServer();
        verify(server, times(1)).listen(eq(port), eq(host), any(Handler.class));

    }

    private JerseyModule createInstance(JsonObject config) {

        JerseyModule module = new JerseyModule();

        Container container = mock(Container.class);
        when(container.config()).thenReturn(config);
        when(container.logger()).thenReturn(mock(Logger.class));

        Vertx vertx = mock(Vertx.class);
        HttpServer server = mock(HttpServer.class);
        when(vertx.createHttpServer()).thenReturn(server);
        when(server.requestHandler(any(Handler.class))).thenReturn(server);

        module.setContainer(container);
        module.setVertx(vertx);

        return module;
    }

//    @Test
//    public void testGetResourceConfig() throws Exception {
//
//    }
}
