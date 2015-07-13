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

package com.englishtown.vertx.jersey.jackson;

import com.englishtown.vertx.jersey.jackson.internal.DefaultObjectMapperConfigurator;
import com.englishtown.vertx.jersey.jackson.internal.JsonObjectMapperProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Feature used to register JsonObject serializers and deserializers for Jackson.
 */
public class JsonObjectFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {

        if(!context.getConfiguration().isRegistered(JsonObjectMapperProvider.class)) {
            context.register(new Binder());
            context.register(JsonObjectMapperProvider.class);
        }

        return true;

    }

    private static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(DefaultObjectMapperConfigurator.class).to(ObjectMapperConfigurator.class).in(Singleton.class);
        }
    }


}
