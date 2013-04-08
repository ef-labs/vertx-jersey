package com.englishtown.vertx.jersey.inject;

import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.VertxParam;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.inject.*;
import org.glassfish.jersey.server.model.Parameter;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.impl.HttpReadStreamBase;
import org.vertx.java.core.streams.ReadStream;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/8/13
 * Time: 12:22 PM
 * Provides Jersey injection for {@link VertxParam} values
 */
@Singleton
public class VertxParamValueFactoryProvider extends AbstractValueFactoryProvider {

    /**
     * {@link InjectionResolver Injection resolver} for {@link javax.ws.rs.BeanParam bean parameters}.
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
                    || HttpReadStreamBase.class.isAssignableFrom(rawType)
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