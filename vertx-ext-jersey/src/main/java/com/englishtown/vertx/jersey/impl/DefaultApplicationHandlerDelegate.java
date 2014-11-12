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
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;

/**
 * Default {@link com.englishtown.vertx.jersey.ApplicationHandlerDelegate} implementation
 */
//TODO Migration: Rename to ApplicationHandlerDelegateImpl
public class DefaultApplicationHandlerDelegate implements ApplicationHandlerDelegate {

    private final ApplicationHandler delegate;

    public DefaultApplicationHandlerDelegate(ApplicationHandler handler) {
        delegate = handler;
    }

    /**
     * The main request/response processing entry point for Jersey container implementations.
     *
     * @param request the Jersey {@link org.glassfish.jersey.server.ContainerRequest} to process
     */
    @Override
    public void handle(ContainerRequest request) {
        delegate.handle(request);
    }

    /**
     * Returns the application handler service locator
     *
     * @return inner service locator
     */
    @Override
    public ServiceLocator getServiceLocator() {
        return delegate.getServiceLocator();
    }

}
