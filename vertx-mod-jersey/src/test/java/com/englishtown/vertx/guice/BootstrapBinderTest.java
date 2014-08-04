package com.englishtown.vertx.guice;

import com.google.inject.Binder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BootstrapBinderTest {

    BootstrapBinder binder = new BootstrapBinder();

    @Mock
    Binder builder;

    @Test
    public void testConfigure() throws Exception {

        binder.configure(builder);
        verify(builder).install(any(GuiceJerseyBinder.class));

    }
}