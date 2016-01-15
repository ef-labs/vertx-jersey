package com.englishtown.vertx.guice;

import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyServerOptions;
import com.englishtown.vertx.jersey.VertxContainer;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GuiceJerseyServerTest {

    GuiceJerseyServer server;

    @Mock
    JerseyHandler handler;
    @Mock
    VertxContainer container;
    @Mock
    JerseyServerOptions options;
    @Mock
    ServiceLocator locator;
    @Mock
    DynamicConfigurationService dcs;
    @Mock
    DynamicConfiguration dc;
    @Mock
    Injector injector;
    @Mock
    GuiceIntoHK2Bridge bridge;

    @Before
    public void setUp() throws Exception {

        when(locator.getService(eq(GuiceIntoHK2Bridge.class))).thenReturn(bridge);
        when(locator.getService(eq(DynamicConfigurationService.class))).thenReturn(dcs);
        when(dcs.createDynamicConfiguration()).thenReturn(dc);

        Key<Set<ContainerRequestFilter>> key = Key.get(new TypeLiteral<Set<ContainerRequestFilter>>() {
        });
        Set<ContainerRequestFilter> set = new LinkedHashSet<>();
        set.add(mock(ContainerRequestFilter.class));

        when(injector.getInstance(Matchers.any(key.getClass()))).thenReturn(set);

        server = new GuiceJerseyServer(handler, container, () -> options, locator, injector);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInjectMultiBindings() throws Exception {

        Key<?> key;

        key = Key.get(new TypeLiteral<Set<ContainerRequestFilter>>() {
        });
        Set<ContainerRequestFilter> requestFilters = (Set<ContainerRequestFilter>) injector.getInstance(key);
        assertEquals(1, requestFilters.size());
        verify(injector, times(2)).getInstance(eq(key));

        key = Key.get(new TypeLiteral<Set<ContainerResponseFilter>>() {
        });
        verify(injector).getInstance(eq(key));

        key = Key.get(new TypeLiteral<Set<ReaderInterceptor>>() {
        });
        verify(injector).getInstance(eq(key));

        key = Key.get(new TypeLiteral<Set<WriterInterceptor>>() {
        });
        verify(injector).getInstance(eq(key));

        key = Key.get(new TypeLiteral<Set<ModelProcessor>>() {
        });
        verify(injector).getInstance(eq(key));

    }

}
