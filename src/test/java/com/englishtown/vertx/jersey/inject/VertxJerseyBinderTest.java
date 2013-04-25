package com.englishtown.vertx.jersey.inject;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.IterableProvider;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/25/13
 * Time: 10:32 AM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("unchecked")
public class VertxJerseyBinderTest {

    @Test
    public void testConfigure() throws Exception {

        VertxJerseyBinder binder = new VertxJerseyBinder();
        DynamicConfiguration dynamicConfiguration = mock(DynamicConfiguration.class);
        binder.bind(dynamicConfiguration);

    }

    @Test
    public void testVertxRequestProcessorFactory() {

        IterableProvider<VertxRequestProcessor> providers = mock(IterableProvider.class);
        VertxJerseyBinder.VertxRequestProcessorFactory factory;
        List<VertxRequestProcessor> result;

        List<VertxRequestProcessor> list = Arrays.asList(
                mock(VertxRequestProcessor.class),
                mock(VertxRequestProcessor.class));

        when(providers.iterator()).thenReturn(list.iterator());

        factory = new VertxJerseyBinder.VertxRequestProcessorFactory(providers);
        result = factory.provide();

        assertNotNull(result);
        assertEquals(2, result.size());

        factory.dispose(result);

    }

    @Test
    public void testVertxResponseProcessorFactory() {

        IterableProvider<VertxResponseProcessor> providers = mock(IterableProvider.class);
        VertxJerseyBinder.VertxResponseProcessorFactory factory;
        List<VertxResponseProcessor> result;

        List<VertxResponseProcessor> list = Arrays.asList(
                mock(VertxResponseProcessor.class),
                mock(VertxResponseProcessor.class));

        when(providers.iterator()).thenReturn(list.iterator());

        factory = new VertxJerseyBinder.VertxResponseProcessorFactory(providers);
        result = factory.provide();

        assertNotNull(result);
        assertEquals(2, result.size());

        factory.dispose(result);

    }

}
