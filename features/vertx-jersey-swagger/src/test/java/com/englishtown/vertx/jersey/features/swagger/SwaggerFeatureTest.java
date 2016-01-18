package com.englishtown.vertx.jersey.features.swagger;

import com.englishtown.vertx.jersey.features.swagger.internal.SwaggerCorsFilter;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SwaggerFeature}
 */
@RunWith(MockitoJUnitRunner.class)
public class SwaggerFeatureTest {

    private SwaggerFeature feature = new SwaggerFeature();
    private Map<String, Object> properties = new HashMap<>();

    @Mock
    private FeatureContext context;
    @Mock
    private Configuration configuration;

    @Before
    public void setUp() throws Exception {

        when(context.getConfiguration()).thenReturn(configuration);
        when(configuration.getProperties()).thenReturn(properties);

    }

    @Test
    public void testConfigure() throws Exception {

        boolean result = feature.configure(context);

        assertTrue(result);
        verify(context).register(eq(SwaggerCorsFilter.class));
        verify(context).register(eq(ApiListingResource.class));
        verify(context).register(eq(SwaggerSerializers.class));

    }

    @Test
    public void testConfigure_Disable() throws Exception {

        properties.put(SwaggerFeature.PROPERTY_DISABLE, true);
        boolean result =feature.configure(context);

        assertFalse(result);
        verify(context, never()).register(any());

    }

}