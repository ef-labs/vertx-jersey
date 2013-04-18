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

package com.englishtown.vertx.jersey.inject;

import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.VertxParam;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.inject.*;
import org.glassfish.jersey.server.model.Parameter;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.streams.ReadStream;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides Jersey injection for {@link VertxParam} values
 */
@Singleton
public class VertxParamValueFactoryProvider extends AbstractValueFactoryProvider {

    /**
     * Injection resolver for {@link VertxParam} annotation.
     */
    @Singleton
    static final class InjectionResolver extends ParamInjectionResolver<VertxParam> {

        /**
         * Creates new resolver.
         */
        public InjectionResolver() {
            super(VertxParamValueFactoryProvider.class);
        }
    }

    private static final class VertxParamValueFactory extends AbstractHttpContextValueFactory<Object> {
        private final Parameter parameter;

        private VertxParamValueFactory(Parameter parameter) {
            this.parameter = parameter;
        }

        @Override
        protected Object get(HttpContext context) {

            Class<?> rawType = parameter.getRawType();

            if (HttpServerRequest.class.isAssignableFrom(rawType)
                    || ReadStream.class.isAssignableFrom(rawType)) {
                return context.getRequestContext().getProperty(JerseyHandler.PROPERTY_NAME_REQUEST);
            }
            if (Vertx.class.isAssignableFrom(rawType)) {
                return context.getRequestContext().getProperty(JerseyHandler.PROPERTY_NAME_VERTX);
            }
            if (Container.class.isAssignableFrom(rawType)) {
                return context.getRequestContext().getProperty(JerseyHandler.PROPERTY_NAME_CONTAINER);
            }

            return null;
        }
    }


    /**
     * Creates new instance initialized from parameters injected by HK2.
     *
     * @param mpep     Multivalued parameter extractor provider.
     * @param injector HK2 Service locator.
     */
    @Inject
    public VertxParamValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator injector) {
        super(mpep, injector, Parameter.Source.UNKNOWN);
    }

    @Override
    public AbstractHttpContextValueFactory<?> createValueFactory(Parameter parameter) {
        return new VertxParamValueFactory(parameter);
    }
}