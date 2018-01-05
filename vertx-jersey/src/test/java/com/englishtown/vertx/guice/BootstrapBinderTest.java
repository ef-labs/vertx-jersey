package com.englishtown.vertx.guice;

import com.google.inject.Binder;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

public class BootstrapBinderTest {

    BootstrapBinder binder = new BootstrapBinder();

    @Mock
    Binder builder;

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testConfigure() throws Exception {

        binder.configure(builder);
        verify(builder).install(any(GuiceJerseyBinder.class));

    }
}