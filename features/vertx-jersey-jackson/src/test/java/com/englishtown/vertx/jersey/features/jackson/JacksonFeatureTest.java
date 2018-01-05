package com.englishtown.vertx.jersey.features.jackson;

import com.englishtown.vertx.jersey.features.jackson.internal.ObjectMapperProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JacksonFeature}
 */
public class JacksonFeatureTest {

    private JacksonFeature feature = new JacksonFeature();
    private Map<String, Object> properties = new HashMap<>();

    @Mock
    private FeatureContext context;
    @Mock
    private Configuration configuration;

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {

        when(context.getConfiguration()).thenReturn(configuration);
        when(configuration.getProperties()).thenReturn(properties);

    }

    @Test
    public void testConfigure() throws Exception {

        boolean result;

        result = feature.configure(context);
        assertTrue(result);

        verify(context).register(Matchers.<JacksonFeature.Binder>any());
        verify(context).register(eq(ObjectMapperProvider.class));

    }

    @Test
    public void testConfigure_Disable() throws Exception {

        properties.put(JacksonFeature.PROPERTY_DISABLE, true);
        boolean result;

        result = feature.configure(context);
        assertFalse(result);

        verify(context, never()).register(Matchers.<JacksonFeature.Binder>any());
        verify(context, never()).register(eq(ObjectMapperProvider.class));

    }

}