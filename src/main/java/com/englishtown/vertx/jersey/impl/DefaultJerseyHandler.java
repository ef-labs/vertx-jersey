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
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyHandlerConfigurator;
import com.englishtown.vertx.jersey.inject.ContainerResponseWriterProvider;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.security.DefaultSecurityContext;
import com.hazelcast.nio.FastByteArrayInputStream;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Vertx {@link HttpServerRequest} handler that delegates handling to Jersey
 */
public class DefaultJerseyHandler implements JerseyHandler {

    private Vertx vertx;
    private Container container;
    private ApplicationHandlerDelegate applicationHandlerDelegate;
    private URI baseUri;
    private final ContainerResponseWriterProvider responseWriterProvider;
    private final JerseyHandlerConfigurator configurator;
    private int maxBodySize;
    private final List<VertxRequestProcessor> requestProcessors;

    @Inject
    public DefaultJerseyHandler(
            ContainerResponseWriterProvider responseWriterProvider,
            List<VertxRequestProcessor> requestProcessors,
            JerseyHandlerConfigurator configurator) {
        this.responseWriterProvider = responseWriterProvider;
        this.requestProcessors = requestProcessors != null ? requestProcessors : new ArrayList<VertxRequestProcessor>();
        this.configurator = configurator;
    }

    @Override
    public void init(Vertx vertx, Container container) {

        this.vertx = vertx;
        this.container = container;

        configurator.init(vertx, container);
        baseUri = configurator.getBaseUri();
        applicationHandlerDelegate = configurator.getApplicationHandler();
        maxBodySize = configurator.getMaxBodySize();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final HttpServerRequest vertxRequest) {

        // Wait for the body for jersey to handle form/json/xml params
        if (shouldReadData(vertxRequest)) {
            final Buffer body = new Buffer();

            vertxRequest.dataHandler(new Handler<Buffer>() {
                public void handle(Buffer buff) {
                    body.appendBuffer(buff);
                    if (body.length() > maxBodySize) {
                        throw new RuntimeException("The input stream has exceeded the max allowed body size "
                                + maxBodySize + ".");
                    }
                }
            });
            vertxRequest.endHandler(new Handler<Void>() {
                @Override
                public void handle(Void event) {
                    InputStream inputStream = new FastByteArrayInputStream(body.getBytes());
                    DefaultJerseyHandler.this.handle(vertxRequest, inputStream);
                }
            });

        } else {
            DefaultJerseyHandler.this.handle(vertxRequest, null);
        }

    }

    protected void handle(
            final HttpServerRequest vertxRequest,
            final InputStream inputStream
    ) {

        String scheme = vertxRequest.absoluteURI() == null ? null : vertxRequest.absoluteURI().getScheme();
        boolean isSecure = "https".equalsIgnoreCase(scheme);

        // Create the jersey request
        final ContainerRequest jerseyRequest = new ContainerRequest(
                baseUri,
                vertxRequest.absoluteURI(),
                vertxRequest.method(),
                new DefaultSecurityContext(isSecure),
                new MapPropertiesDelegate());

        handle(vertxRequest, inputStream, jerseyRequest);

    }

    protected void handle(final HttpServerRequest vertxRequest,
                          final InputStream inputStream,
                          final ContainerRequest jerseyRequest) {

        // Provide the vertx response writer
        jerseyRequest.setWriter(responseWriterProvider.get(vertxRequest, jerseyRequest));

        // Set entity stream if provided (form posts)
        if (inputStream != null) {
            jerseyRequest.setEntityStream(inputStream);
        }

        // Copy headers
        for (Map.Entry<String, String> header : vertxRequest.headers().entries()) {
            jerseyRequest.getHeaders().add(header.getKey(), header.getValue());
        }

        // Set request scoped instances
        jerseyRequest.setRequestScopedInitializer(new RequestScopedInitializer() {
            @Override
            public void initialize(ServiceLocator locator) {
                locator.<Ref<Vertx>>getService((new TypeLiteral<Ref<Vertx>>() {
                }).getType()).set(vertx);
                locator.<Ref<Container>>getService((new TypeLiteral<Ref<Container>>() {
                }).getType()).set(container);
                locator.<Ref<HttpServerRequest>>getService((new TypeLiteral<Ref<HttpServerRequest>>() {
                }).getType()).set(vertxRequest);
                locator.<Ref<HttpServerResponse>>getService((new TypeLiteral<Ref<HttpServerResponse>>() {
                }).getType()).set(vertxRequest.response());
            }
        });

        // Call vertx before request processors
        if (requestProcessors.size() > 0) {
            callVertxRequestProcessor(0, vertxRequest, jerseyRequest, new Handler<Void>() {
                @Override
                public void handle(Void aVoid) {
                    applicationHandlerDelegate.handle(jerseyRequest);
                }
            });
        } else {
            applicationHandlerDelegate.handle(jerseyRequest);
        }

    }

    protected void callVertxRequestProcessor(
            int index,
            final HttpServerRequest vertxRequest,
            final ContainerRequest jerseyRequest,
            final Handler<Void> done
    ) {

        if (index >= requestProcessors.size()) {
            done.handle(null);
        }

        VertxRequestProcessor processor = requestProcessors.get(index);
        final int next = index + 1;

        processor.process(vertxRequest, jerseyRequest, new Handler<Void>() {
            @Override
            public void handle(Void aVoid) {
                if (next >= requestProcessors.size()) {
                    done.handle(null);
                } else {
                    callVertxRequestProcessor(next, vertxRequest, jerseyRequest, done);
                }
            }
        });

    }

    protected boolean shouldReadData(HttpServerRequest vertxRequest) {

        String method = vertxRequest.method();

        // Only read input stream data for post/put methods
        if (!(HttpMethod.POST.equalsIgnoreCase(method) || HttpMethod.PUT.equalsIgnoreCase(method))) {
            return false;
        }

        String contentType = vertxRequest.headers().get(HttpHeaders.CONTENT_TYPE);

        // Only read input stream data for content types form/json/xml
        return MediaType.APPLICATION_FORM_URLENCODED.equalsIgnoreCase(contentType)
                || MediaType.APPLICATION_JSON.equalsIgnoreCase(contentType)
                || MediaType.APPLICATION_XML.equalsIgnoreCase(contentType);
    }

}
