package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;
import com.englishtown.vertx.jersey.JerseyHandlerConfigurator;
import com.englishtown.vertx.jersey.inject.ContainerResponseWriterProvider;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.impl.HttpHeadersAdapter;
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
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link DefaultJerseyHandler} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJerseyHandlerTest {

    @Mock
    JerseyHandlerConfigurator configurator;
    @Mock
    ApplicationHandlerDelegate applicationHandlerDelegate;

    @Before
    public void setUp() {

        when(configurator.getApplicationHandler()).thenReturn(applicationHandlerDelegate);
        when(configurator.getMaxBodySize()).thenReturn(1024);

    }

    @Test
    public void testHandle() throws Exception {

        DefaultJerseyHandler handler = createInstance();
        HttpServerRequest request = mock(HttpServerRequest.class);
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(request.absoluteURI()).thenReturn(URI.create("http://test.englishtown.com/test"));
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.response()).thenReturn(response);
        when(request.headers()).thenReturn(mock(MultiMap.class));

        handler.handle(request);
        verify(applicationHandlerDelegate).handle(any(ContainerRequest.class));

    }

    private DefaultJerseyHandler createInstance() {
        VertxRequestProcessor[] processors = null;
        return createInstance(processors);
    }

    private DefaultJerseyHandler createInstance(VertxRequestProcessor... requestProcessors) {

        Vertx vertx = mock(Vertx.class);
        Container container = mock(Container.class);
        List<VertxRequestProcessor> processors = requestProcessors != null ? Arrays.asList(requestProcessors) : null;

        JsonObject config = new JsonObject();
        Logger logger = mock(Logger.class);
        when(container.config()).thenReturn(config);
        when(container.logger()).thenReturn(logger);

        JsonArray resources = new JsonArray().addString("com.englishtown.vertx.jersey.resources");
        config.putArray(DefaultJerseyHandlerConfigurator.CONFIG_RESOURCES, resources);

        ContainerResponseWriterProvider provider = mock(ContainerResponseWriterProvider.class);
        when(provider.get(any(HttpServerRequest.class), any(ContainerRequest.class))).thenReturn(mock
                (ContainerResponseWriter.class));

        DefaultJerseyHandler handler = new DefaultJerseyHandler(provider, processors, configurator);
        handler.init(vertx, container);

        return handler;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandle_JSON_POST() throws Exception {

        DefaultJerseyHandler handler = createInstance();
        final HttpServerRequest request = mock(HttpServerRequest.class);

        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        when(request.method()).thenReturn(HttpMethod.POST);
        when(request.headers()).thenReturn(new HttpHeadersAdapter(headers));

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

        verify(applicationHandlerDelegate).handle(any(ContainerRequest.class));

    }

    @Test
    public void testHandle_VertxRequestHandler() throws Exception {

        DefaultJerseyHandler handler = createInstance(
                new VertxRequestProcessor() {
                    @Override
                    public void process(HttpServerRequest vertxRequest, ContainerRequest jerseyRequest, Handler<Void> done) {
                        done.handle(null);
                    }
                },
                new VertxRequestProcessor() {
                    @Override
                    public void process(HttpServerRequest vertxRequest, ContainerRequest jerseyRequest, Handler<Void> done) {
                        done.handle(null);
                    }
                }
        );

        HttpServerRequest request = mock(HttpServerRequest.class);
        when(request.headers()).thenReturn(mock(MultiMap.class));
        InputStream inputStream = null;

        handler.handle(request, inputStream);
        verify(applicationHandlerDelegate).handle(any(ContainerRequest.class));

    }

    @Test
    public void testShouldReadData() throws Exception {

        DefaultJerseyHandler handler = createInstance();
        HttpServerRequest request = mock(HttpServerRequest.class);
        boolean result;

        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);

        when(request.method()).thenReturn(HttpMethod.GET).thenReturn(HttpMethod.PUT);
        when(request.headers()).thenReturn(new HttpHeadersAdapter(headers));

        result = handler.shouldReadData(request);
        assertFalse(result);

        result = handler.shouldReadData(request);
        assertFalse(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        result = handler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        when(request.method()).thenReturn(HttpMethod.POST);

        result = handler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);

        result = handler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED + "; charset=UTF-8");

        result = handler.shouldReadData(request);
        assertTrue(result);

    }
}
