package com.englishtown.vertx.hk2;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/25/13
 * Time: 10:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class BootstrapBinderTest {

    @Test
    public void testConfigure() throws Exception {

        BootstrapBinder binder = new BootstrapBinder();
        DynamicConfiguration dynamicConfiguration = mock(DynamicConfiguration.class);
        binder.bind(dynamicConfiguration);

    }

}
