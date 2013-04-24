package com.englishtown.vertx.jersey;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import javax.inject.Provider;

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
@SuppressWarnings("unchecked")
public class JerseyModuleTest {

    @Test
    public void testStart() throws Exception {

//        JsonObject config = new JsonObject()
//                .putArray(JerseyModule.CONFIG_RESOURCES, new JsonArray().addString("com.englishtown.vertx.resources"));

        JerseyModule module = createInstance(new JsonObject());
        Vertx vertx = module.getVertx();
        Future<Void> startedResult = mock(Future.class);

        module.start(startedResult);

        verify(vertx, times(1)).createHttpServer();
        HttpServer server = vertx.createHttpServer();
        verify(server, times(1)).listen(eq(80), eq("0.0.0.0"), any(Handler.class));

    }

    @Test
    public void testStart_No_Handler() throws Exception {

        Provider<JerseyHandler> provider = mock(Provider.class);
        JerseyModule module = new JerseyModule(provider);
        JsonObject config = new JsonObject();
        Container container = mock(Container.class);
        when(container.config()).thenReturn(config);
        module.setVertx(mock(Vertx.class));
        module.setContainer(container);
        Future<Void> startedResult = mock(Future.class);

        try {
            module.start(startedResult);
            fail();
        } catch (IllegalStateException e) {
            // Expected
            assertEquals("A JerseyHandlerProvider has not been configured", e.getMessage());
        }

    }

    @Test
    public void testStart_Config() throws Exception {

        String host = "test.englishtown.com";
        int port = 8888;
        int bufferSize = 1024;

        JsonObject config = new JsonObject()
                .putString(JerseyModule.CONFIG_HOST, host)
                .putNumber(JerseyModule.CONFIG_PORT, port)
                .putNumber(JerseyModule.CONFIG_RECEIVE_BUFFER_SIZE, bufferSize);

        JerseyModule module = createInstance(config);
        Vertx vertx = module.getVertx();
        Future<Void> startedResult = mock(Future.class);

        module.start(startedResult);

        verify(vertx, times(1)).createHttpServer();
        HttpServer server = vertx.createHttpServer();
        verify(server, times(1)).listen(eq(port), eq(host), any(Handler.class));

    }

    @Test
    public void testListenResult() throws Exception {

        JerseyModule module = createInstance(new JsonObject());
        Vertx vertx = module.getVertx();
        Future<Void> startedResult = mock(Future.class);
        final Handler<AsyncResult<HttpServer>>[] listenHandler = new Handler[1];

        final HttpServer server = vertx.createHttpServer();
        when(server.listen(anyInt(), anyString(), any(Handler.class))).thenAnswer(new Answer<HttpServer>() {
            @Override
            public HttpServer answer(InvocationOnMock invocation) throws Throwable {
                listenHandler[0] = (Handler<AsyncResult<HttpServer>>) invocation.getArguments()[2];
                return server;
            }
        });

        module.start(startedResult);

        AsyncResult<HttpServer> result = mock(AsyncResult.class);
        when(result.succeeded()).thenReturn(true).thenReturn(false);

        listenHandler[0].handle(result);
        verify(startedResult, times(1)).setResult(null);

        listenHandler[0].handle(result);
        verify(startedResult, times(1)).setFailure(any(Throwable.class));

    }

    private JerseyModule createInstance(JsonObject config) {

        Provider<JerseyHandler> provider = mock(Provider.class);
        when(provider.get()).thenReturn(mock(JerseyHandler.class));
        JerseyModule module = new JerseyModule(provider);

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

}
