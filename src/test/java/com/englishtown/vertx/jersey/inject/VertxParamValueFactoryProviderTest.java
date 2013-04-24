package com.englishtown.vertx.jersey.inject;

import com.englishtown.vertx.jersey.DefaultJerseyHandler;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractHttpContextValueFactory;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.streams.ReadStream;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

import javax.inject.Provider;
import java.lang.reflect.Field;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;


/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/18/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("unchecked")
public class VertxParamValueFactoryProviderTest {
    @Test
    public void testCreateValueFactory_HttpServerRequest() throws Exception {

        VertxParamValueFactoryProvider provider = createInstance();
        Parameter parameter = createParameter(HttpServerRequest.class);

        Factory<?> factory = provider.getValueFactory(parameter);
        Object o = factory.provide();
        assert (o instanceof HttpServerRequest);

    }

    @Test
    public void testCreateValueFactory_ReadStream() throws Exception {

        VertxParamValueFactoryProvider provider = createInstance();
        Parameter parameter = createParameter(ReadStream.class);

        Factory<?> factory = provider.getValueFactory(parameter);
        Object o = factory.provide();
        assert (o instanceof ReadStream);

    }

    @Test
    public void testCreateValueFactory_Container() throws Exception {

        VertxParamValueFactoryProvider provider = createInstance();
        Parameter parameter = createParameter(Container.class);

        Factory<?> factory = provider.getValueFactory(parameter);
        Object o = factory.provide();
        assert (o instanceof Container);

    }

    @Test
    public void testCreateValueFactory_Vertx() throws Exception {

        VertxParamValueFactoryProvider provider = createInstance();
        Parameter parameter = createParameter(Vertx.class);

        Factory<?> factory = provider.getValueFactory(parameter);
        Object o = factory.provide();
        assert (o instanceof Vertx);

    }

    @Test
    public void testCreateValueFactory_Invalid() throws Exception {

        VertxParamValueFactoryProvider provider = createInstance();
        Parameter parameter = createParameter(Verticle.class);

        Factory<?> factory = provider.getValueFactory(parameter);
        Object o = factory.provide();
        assertNull(o);

    }

    private VertxParamValueFactoryProvider createInstance() {

        MultivaluedParameterExtractorProvider mpep = mock(MultivaluedParameterExtractorProvider.class);
        ServiceLocator locator = mock(ServiceLocator.class);

        final Provider<ContainerRequest> requestProvider = mock(Provider.class);
        ContainerRequest containerRequest = mock(ContainerRequest.class);
        when(requestProvider.get()).thenReturn(containerRequest);

        when(containerRequest.getProperty(eq(DefaultJerseyHandler.PROPERTY_NAME_CONTAINER)))
                .thenReturn(mock(Container.class));
        when(containerRequest.getProperty(eq(DefaultJerseyHandler.PROPERTY_NAME_REQUEST)))
                .thenReturn(mock(HttpServerRequest.class));
        when(containerRequest.getProperty(eq(DefaultJerseyHandler.PROPERTY_NAME_VERTX)))
                .thenReturn(mock(Vertx.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Object obj = args[0];
                Field field = AbstractHttpContextValueFactory.class.getDeclaredField("request");
                field.setAccessible(true);
                field.set(obj, requestProvider);
                return null;
            }
        }).when(locator).inject(anyObject());

        return new VertxParamValueFactoryProvider(mpep, locator);
    }

    private Parameter createParameter(Class<?> rawType) {
        Parameter parameter = mock(Parameter.class);

        Mockito.<Class<?>>when(parameter.getRawType()).thenReturn(rawType);
        when(parameter.getSource()).thenReturn(Parameter.Source.UNKNOWN);

        return parameter;
    }

}
