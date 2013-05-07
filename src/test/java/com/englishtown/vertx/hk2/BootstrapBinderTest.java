package com.englishtown.vertx.hk2;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * BootStrapBinder unit tests
 */
public class BootstrapBinderTest {

    @Test
    public void testConfigure() throws Exception {

        BootstrapBinder binder = new BootstrapBinder();
        DynamicConfiguration dynamicConfiguration = mock(DynamicConfiguration.class);
        binder.bind(dynamicConfiguration);

    }

}
