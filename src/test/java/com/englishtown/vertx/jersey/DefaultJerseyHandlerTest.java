package com.englishtown.vertx.jersey;

import com.englishtown.vertx.jersey.inject.ContainerResponseWriterProvider;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
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
@SuppressWarnings("unchecked")
public class DefaultJerseyHandlerTest {

    private static class TestDefaultJerseyHandler extends DefaultJerseyHandler {

        public boolean done;
        public ContainerRequest request;

        public TestDefaultJerseyHandler(
                ContainerResponseWriterProvider responseWriterProvider,
                List<VertxRequestProcessor> requestProcessors) {
            super(responseWriterProvider, requestProcessors);
        }

        @Override
        void handle(ContainerRequest jerseyRequest) {
            this.request = jerseyRequest;
            done = true;
        }
    }

    @Test
    public void testGet_No_Config() throws Exception {

        DefaultJerseyHandler handler = new DefaultJerseyHandler(null, null);

        try {
            handler.init(mock(Vertx.class), mock(Container.class));
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }

    }

    @Test
    public void testStart_Missing_Resources() throws Exception {

        DefaultJerseyHandler provider = new DefaultJerseyHandler(null, null);

        try {
            provider.getResourceConfig(new JsonObject());
            fail();

        } catch (RuntimeException e) {
            assertEquals(e.getMessage(), "At lease one resource package name must be specified in the config " +
                    "resources");

        }

    }

    @Test
    public void testGetBaseUri() throws Exception {

        DefaultJerseyHandler provider = createInstance();
        JsonObject config = new JsonObject();
        URI uri;
        String expected = "/";

        uri = provider.getBaseUri(config);
        assertEquals(expected, uri.getPath());

        expected = "test/base/path";
        config.putString(DefaultJerseyHandler.CONFIG_BASE_PATH, expected);
        uri = provider.getBaseUri(config);
        assertEquals(expected, uri.getPath());

    }

    @Test
    public void testGetResourceConfig() throws Exception {

        DefaultJerseyHandler provider = createInstance();
        JsonObject config = new JsonObject();
        ResourceConfig resourceConfig;

        try {
            provider.getResourceConfig(config);
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

        JsonArray resources = new JsonArray().addString("com.englishtown.vertx.jersey.resources");
        config.putArray(DefaultJerseyHandler.CONFIG_RESOURCES, resources);
        resourceConfig = provider.getResourceConfig(config);

        assertNotNull(resourceConfig);
        assertEquals(1, resourceConfig.getClasses().size());
        assertEquals(0, resourceConfig.getInstances().size());

        JsonArray features = new JsonArray().addString("com.englishtown.vertx.jersey.inject.TestFeature");
        config.putArray(DefaultJerseyHandler.CONFIG_FEATURES, features);
        resourceConfig = provider.getResourceConfig(config);

        assertNotNull(resourceConfig);
        assertEquals(2, resourceConfig.getClasses().size());

        JsonArray binders = new JsonArray().addString("com.englishtown.vertx.jersey.inject.TestBinder2");
        config.putArray(DefaultJerseyHandler.CONFIG_BINDERS, binders);
        resourceConfig = provider.getResourceConfig(config);

        assertNotNull(resourceConfig);
        assertEquals(2, resourceConfig.getClasses().size());
        assertEquals(1, resourceConfig.getInstances().size());

        binders.addString("com.englishtown.vertx.jersey.inject.ClassNotFoundBinder");
        try {
            provider.getResourceConfig(config);
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

        features.addString("com.englishtown.vertx.jersey.inject.ClassNotFoundFeature");
        try {
            provider.getResourceConfig(config);
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

    }

    @Test
    public void testHandle() throws Exception {

        TestDefaultJerseyHandler handler = createInstance();
        HttpServerRequest request = mock(HttpServerRequest.class);
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(request.absoluteURI()).thenReturn(URI.create("http://test.englishtown.com/test"));
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.response()).thenReturn(response);

        handler.handle(request);
        assertTrue(handler.done);

    }

    private TestDefaultJerseyHandler createInstance() {
        VertxRequestProcessor[] processors = null;
        return createInstance(processors);
    }

    private TestDefaultJerseyHandler createInstance(VertxRequestProcessor... requestProcessors) {

        Vertx vertx = mock(Vertx.class);
        Container container = mock(Container.class);
        List<VertxRequestProcessor> processors = requestProcessors != null ? Arrays.asList(requestProcessors) : null;

        JsonObject config = new JsonObject();
        Logger logger = mock(Logger.class);
        when(container.config()).thenReturn(config);
        when(container.logger()).thenReturn(logger);

        JsonArray resources = new JsonArray().addString("com.englishtown.vertx.jersey.resources");
        config.putArray(DefaultJerseyHandler.CONFIG_RESOURCES, resources);

        ContainerResponseWriterProvider provider = mock(ContainerResponseWriterProvider.class);
        when(provider.get(any(HttpServerRequest.class), any(ContainerRequest.class))).thenReturn(mock
                (ContainerResponseWriter.class));

        TestDefaultJerseyHandler handler = new TestDefaultJerseyHandler(provider, processors);
        handler.init(vertx, container);

        return handler;
    }

    @Test
    public void testHandle_JSON_POST() throws Exception {

        TestDefaultJerseyHandler handler = createInstance();
        final HttpServerRequest request = mock(HttpServerRequest.class);

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

        handler.handle(request);

        Buffer data = new Buffer("{}");
        dataHandler[0].handle(data);
        endHandler[0].handle(null);

        assertTrue(handler.done);

    }

    @Test
    public void testHandle_VertxRequestHandler() throws Exception {

        TestDefaultJerseyHandler handler = createInstance(
                new VertxRequestProcessor() {
                    @Override
                    public void handle(HttpServerRequest vertxRequest, ContainerRequest jerseyRequest, Handler<Void> done) {
                        done.handle(null);
                    }
                },
                new VertxRequestProcessor() {
                    @Override
                    public void handle(HttpServerRequest vertxRequest, ContainerRequest jerseyRequest, Handler<Void> done) {
                        done.handle(null);
                    }
                }
        );

        HttpServerRequest request = mock(HttpServerRequest.class);
        InputStream inputStream = null;

        handler.handle(request, inputStream);
        assertTrue(handler.done);

    }

    @Test
    public void testShouldReadData() throws Exception {

        DefaultJerseyHandler handler = createInstance();
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
