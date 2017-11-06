package com.englishtown.vertx.jersey.features.swagger.internal;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Configuration;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static io.swagger.jaxrs.config.SwaggerContextService.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SwaggerServletConfig}
 */
public class SwaggerServletConfigTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private final String configId = "configId";
    private final String scannerId = "scannerId";
    private final String contextId = "contextId";

    private SwaggerServletConfig servletConfig;
    private Map<String, Object> properties = new HashMap<>();

    @Mock
    private ServletContext context;
    @Mock
    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        properties.put(CONFIG_ID_KEY, configId);
        properties.put(SCANNER_ID_KEY, scannerId);
        properties.put(CONTEXT_ID_KEY, contextId);

        when(context.getContextPath()).thenReturn("basePath");
        when(configuration.getProperties()).thenReturn(properties);
    }

    @Test
    public void testInitParamsCreation() throws Exception {
        servletConfig = new SwaggerServletConfig(context, configuration);

        assertNotNull(servletConfig);

        assertEquals(configId, servletConfig.getInitParameter(CONFIG_ID_KEY));
        assertEquals(scannerId, servletConfig.getInitParameter(SCANNER_ID_KEY));
        assertEquals(contextId, servletConfig.getInitParameter(CONTEXT_ID_KEY));
    }

    @Test
    public void testInitParamsCreation_nullProperty() throws Exception {
        properties.put(CONFIG_ID_KEY, null);

        servletConfig = new SwaggerServletConfig(context, configuration);

        assertNotNull(servletConfig);

        assertNull(servletConfig.getInitParameter(CONFIG_ID_KEY));
    }

    @Test
    public void testInitParamsCreation_ignoredProperty() throws Exception {
        String ignoredKey = "ignore";
        properties.put(ignoredKey, "test-value");

        servletConfig = new SwaggerServletConfig(context, configuration);

        assertNotNull(servletConfig);

        assertNull(servletConfig.getInitParameter(ignoredKey));
    }

    @Test
    public void testGetInitParameterNames() throws Exception {
        servletConfig = new SwaggerServletConfig(context, configuration);

        Enumeration<String> paramNames = servletConfig.getInitParameterNames();
        assertNotNull(paramNames);

        while (paramNames.hasMoreElements()) {
            assertTrue(properties.keySet().contains(paramNames.nextElement()));
        }
    }
}
