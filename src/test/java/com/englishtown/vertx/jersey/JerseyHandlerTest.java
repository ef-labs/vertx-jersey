package com.englishtown.vertx.jersey;

import com.englishtown.vertx.jersey.inject.VertxRequestHandler;
import com.englishtown.vertx.jersey.security.DefaultSecurityContextProvider;
import com.englishtown.vertx.jersey.security.SecurityContextProvider;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/18/13
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class JerseyHandlerTest {

    private static class TestJerseyHandler extends JerseyHandler {

        public boolean done;
        public ContainerRequest request;

        public TestJerseyHandler(Vertx vertx, Container container, URI baseUri, ApplicationHandler application,
                                 SecurityContextProvider securityContextProvider,
                                 List<VertxRequestHandler> vertxHandlers) {
            super(vertx, container, baseUri, application, securityContextProvider, vertxHandlers);
        }

        @Override
        void handle(ContainerRequest jerseyRequest) {
            this.request = jerseyRequest;
            done = true;
        }
    }

    @Test
    public void testHandle() throws Exception {

        TestJerseyHandler handler = createInstance();
        HttpServerRequest request = mock(HttpServerRequest.class);
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(request.absoluteURI()).thenReturn(URI.create("http://test.englishtown.com/test"));
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.response()).thenReturn(response);

        handler.handle(request);
        assertTrue(handler.done);

    }

    private TestJerseyHandler createInstance() {
        return createInstance(null);
    }

    private TestJerseyHandler createInstance(VertxRequestHandler... vertxRequestHandlers) {

        Vertx vertx = mock(Vertx.class);
        Container container = mock(Container.class);
        URI baseUri = URI.create("/");
        ApplicationHandler application = new ApplicationHandler();
        SecurityContextProvider securityContextProvider = new DefaultSecurityContextProvider();
        List<VertxRequestHandler> handlers = vertxRequestHandlers == null ? null : Arrays.asList(vertxRequestHandlers);

        JsonObject config = new JsonObject();
        Logger logger = mock(Logger.class);
        when(container.config()).thenReturn(config);
        when(container.logger()).thenReturn(logger);

        return new TestJerseyHandler(vertx, container, baseUri, application, securityContextProvider, handlers);
    }

    @Test
    public void testHandle_JSON_POST() throws Exception {

        TestJerseyHandler handler = createInstance();
        final HttpServerRequest request = mock(HttpServerRequest.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        when(request.method()).thenReturn(HttpMethod.POST);
        when(request.headers()).thenReturn(headers);

        final Handler<Buffer>[] dataHandler = new Handler[1];
        when(request.dataHandler(any(Handler.class))).thenAnswer(new Answer<HttpServerRequest>() {
            @Override
            public HttpServerRequest answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                dataHandler[0] = (Handler<Buffer>) args[0];
                return request;
            }
        });

        final Handler<Void>[] endHandler = new Handler[1];
        when(request.endHandler(any(Handler.class))).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                endHandler[0] = (Handler<Void>) args[0];
                return null;
            }
        });

        handler.handle(request, securityContext);

        Buffer data = new Buffer("{}");
        dataHandler[0].handle(data);
        endHandler[0].handle(null);

        assertTrue(handler.done);

    }

    @Test
    public void testHandle_VertxRequestHandler() throws Exception {

        TestJerseyHandler handler = createInstance(
                new VertxRequestHandler() {
                    @Override
                    public void handle(HttpServerRequest request, Map<String, Object> properties, Handler<Void> done) {
                        done.handle(null);
                    }
                },
                new VertxRequestHandler() {
                    @Override
                    public void handle(HttpServerRequest request, Map<String, Object> properties, Handler<Void> done) {
                        done.handle(null);
                    }
                }
        );

        HttpServerRequest request = mock(HttpServerRequest.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        InputStream inputStream = null;

        handler.handle(request, securityContext, inputStream);
        assertTrue(handler.done);

    }

    @Test
    public void testShouldReadData() throws Exception {

        JerseyHandler handler = createInstance();
        HttpServerRequest request = mock(HttpServerRequest.class);
        boolean result;

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);

        when(request.method()).thenReturn(HttpMethod.GET).thenReturn(HttpMethod.PUT);
        when(request.headers()).thenReturn(headers);

        result = handler.shouldReadData(request);
        assertFalse(result);

        result = handler.shouldReadData(request);
        assertFalse(result);

        headers.clear();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        result = handler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        when(request.method()).thenReturn(HttpMethod.POST);

        result = handler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);

        result = handler.shouldReadData(request);
        assertTrue(result);

    }
}
