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

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferFactoryImpl;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;

import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;
import com.englishtown.vertx.jersey.JerseyConfigurator;
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.inject.ContainerResponseWriterProvider;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.security.DefaultSecurityContext;

/**
 * The Vertx {@link HttpServerRequest} handler that delegates handling to Jersey
 */
public class DefaultJerseyHandler implements JerseyHandler {

    private ApplicationHandlerDelegate applicationHandlerDelegate;
    private URI baseUri;
    private int maxBodySize;

    private final ContainerResponseWriterProvider responseWriterProvider;
    private final List<VertxRequestProcessor> requestProcessors;
    private static final Logger logger = LoggerFactory.getLogger(DefaultJerseyHandler.class);

    @Inject
    public DefaultJerseyHandler(
            ContainerResponseWriterProvider responseWriterProvider,
            List<VertxRequestProcessor> requestProcessors) {
        this.responseWriterProvider = responseWriterProvider;
        this.requestProcessors = requestProcessors;
    }

    @Override
    public void init(JerseyConfigurator configurator) {
        baseUri = configurator.getBaseUri();
        maxBodySize = configurator.getMaxBodySize();
        applicationHandlerDelegate = configurator.getApplicationHandler();

        logger.debug("DefaultJerseyHandler - initialized");
    }

    @Override
    public URI getBaseUri() {
        if (baseUri == null) {
            throw new IllegalStateException("baseUri is null, have you called init() first?");
        }
        return baseUri;
    }

    @Override
    public ApplicationHandlerDelegate getDelegate() {
        return applicationHandlerDelegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final HttpServerRequest vertxRequest) {

        // Wait for the body for jersey to handle form/json/xml params
        if (shouldReadData(vertxRequest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("DefaultJerseyHandler - handle request and read body: " + vertxRequest.method() + " " + vertxRequest.uri());
            }
            final Buffer body = new BufferFactoryImpl().buffer();

            vertxRequest.handler(buffer -> {
                body.appendBuffer(buffer);
                if (body.length() > maxBodySize) {
                    throw new RuntimeException("The input stream has exceeded the max allowed body size "
                            + maxBodySize + ".");
                }
            });
            vertxRequest.endHandler((Void) -> {
                InputStream inputStream = new ByteArrayInputStream(body.getBytes());
                DefaultJerseyHandler.this.handle(vertxRequest, inputStream);
            });

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("DefaultJerseyHandler - handle request: " + vertxRequest.method() + " " + vertxRequest.uri());
            }
            DefaultJerseyHandler.this.handle(vertxRequest, null);
        }

    }

    protected void handle(
            final HttpServerRequest vertxRequest,
            final InputStream inputStream
    ) {

        URI uri = getAbsoluteURI(vertxRequest);
        boolean isSecure = "https".equalsIgnoreCase(uri.getScheme());

        UriBuilder baseUriBuilder = UriBuilder.fromUri(uri)
                .replacePath(baseUri.getPath())
                .replaceQuery(null);

        // Create the jersey request
        //TODO Migration: ContainerRequest should use the new HttpMethod enum
        final ContainerRequest jerseyRequest = new ContainerRequest(
                baseUriBuilder.build(),
                uri,
                vertxRequest.method().name(),
                new DefaultSecurityContext(isSecure),
                new MapPropertiesDelegate());

        handle(vertxRequest, inputStream, jerseyRequest);

    }

    protected URI getAbsoluteURI(HttpServerRequest vertxRequest) {

        URI absoluteUri;
        String hostAndPort = vertxRequest.headers().get(HttpHeaders.HOST);

        try {
            absoluteUri = UriBuilder.fromUri(vertxRequest.absoluteURI()).build();

            if (hostAndPort != null && !hostAndPort.isEmpty()) {
                String[] parts = hostAndPort.split(":");
                String host = parts[0];
                int port = (parts.length > 1 ? Integer.valueOf(parts[1]) : -1);

                if (!host.equalsIgnoreCase(absoluteUri.getHost()) || port != absoluteUri.getPort()) {
                    absoluteUri = UriBuilder.fromUri(absoluteUri)
                            .host(host)
                            .port(port)
                            .build();
                }
            }

            return absoluteUri;
        } catch (IllegalArgumentException e) {
            String uri = vertxRequest.uri();

            if (!uri.contains("?")) {
                throw e;
            }

            try {
                logger.warn("Invalid URI: " + uri + ".  Attempting to parse query string.", e);
                QueryStringDecoder decoder = new QueryStringDecoder(uri);

                StringBuilder sb = new StringBuilder(decoder.path() + "?");

                for (Map.Entry<String, List<String>> p : decoder.parameters().entrySet()) {
                    for (String value : p.getValue()) {
                        sb.append(p.getKey()).append("=").append(URLEncoder.encode(value, "UTF-8"));
                    }
                }

                return URI.create(sb.toString());

            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }

        }

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
                locator.<Ref<HttpServerRequest>>getService((new TypeLiteral<Ref<HttpServerRequest>>() {
                }).getType()).set(vertxRequest);
                locator.<Ref<HttpServerResponse>>getService((new TypeLiteral<Ref<HttpServerResponse>>() {
                }).getType()).set(vertxRequest.response());
            }
        });

        // Call vertx before request processors
        if (!requestProcessors.isEmpty()) {
            // Pause the vert.x request if we haven't already read the body
            if (inputStream == null) {
                vertxRequest.pause();
            }
            callVertxRequestProcessor(0, vertxRequest, jerseyRequest, new Handler<Void>() {
                @Override
                public void handle(Void aVoid) {
                    // Resume the vert.x request if we haven't already read the body
                    if (inputStream == null) {
                        vertxRequest.resume();
                    }
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

        processor.process(vertxRequest, jerseyRequest, (Void) -> {
            if (next >= requestProcessors.size()) {
                done.handle(null);
            } else {
                callVertxRequestProcessor(next, vertxRequest, jerseyRequest, done);
            }
        });

    }

    protected boolean shouldReadData(HttpServerRequest vertxRequest) {

        HttpMethod method = vertxRequest.method();

        // Only read input stream data for post/put methods
        if (!(HttpMethod.POST == method || HttpMethod.PUT == method)) {
            return false;
        }

        String contentType = vertxRequest.headers().get(HttpHeaders.CONTENT_TYPE);

        if (contentType == null || contentType.isEmpty()) {
            // Special handling for IE8 XDomainRequest where content-type is missing
            // http://blogs.msdn.com/b/ieinternals/archive/2010/05/13/xdomainrequest-restrictions-limitations-and-workarounds.aspx
            return true;
        }

        MediaType mediaType = MediaType.valueOf(contentType);

        // Allow text/plain
        if (MediaType.TEXT_PLAIN_TYPE.getType().equals(mediaType.getType())
                && MediaType.TEXT_PLAIN_TYPE.getSubtype().equals(mediaType.getSubtype())) {
            return true;
        }

        // Only other media types accepted are application (will check subtypes next)
        String applicationType = MediaType.APPLICATION_FORM_URLENCODED_TYPE.getType();
        if (!applicationType.equalsIgnoreCase(mediaType.getType())) {
            return false;
        }

        // Need to do some special handling for forms:
        // Jersey doesn't properly handle when charset is included
        if (mediaType.getSubtype().equalsIgnoreCase(MediaType.APPLICATION_FORM_URLENCODED_TYPE.getSubtype())) {
            if (!mediaType.getParameters().isEmpty()) {
                vertxRequest.headers().remove(HttpHeaders.CONTENT_TYPE);
                vertxRequest.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
            }
            return true;
        }

        // Also accept json/xml sub types
        return MediaType.APPLICATION_JSON_TYPE.getSubtype().equalsIgnoreCase(mediaType.getSubtype())
                || MediaType.APPLICATION_XML_TYPE.getSubtype().equalsIgnoreCase(mediaType.getSubtype());
    }

}
