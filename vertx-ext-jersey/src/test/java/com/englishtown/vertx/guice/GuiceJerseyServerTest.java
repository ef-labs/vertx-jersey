package com.englishtown.vertx.guice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.englishtown.vertx.jersey.JerseyHandler;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

@RunWith(MockitoJUnitRunner.class)
public class GuiceJerseyServerTest {

    GuiceJerseyServer server;

    @Mock
    Provider<JerseyHandler> handlerProvider;
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

        //TODO Migration: Fix generic type inference
        Mockito.when(injector.getInstance((Key<?>)any(key.getClass()))).thenReturn(null);

        server = new GuiceJerseyServer(handlerProvider, locator, injector);
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