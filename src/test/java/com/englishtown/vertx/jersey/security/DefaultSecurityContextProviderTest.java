package com.englishtown.vertx.jersey.security;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import javax.ws.rs.core.SecurityContext;
import java.net.URI;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/18/13
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultSecurityContextProviderTest {

    @Test
    public void testGetSecurityContext() throws Exception {

        DefaultSecurityContextProvider provider = new DefaultSecurityContextProvider();
        HttpServerRequest request = mock(HttpServerRequest.class);
        URI uri = URI.create("http://test.englishtown.com/test");
        when(request.absoluteURI()).thenReturn(uri);
        final boolean[] done = {false};

        provider.getSecurityContext(request, new Handler<SecurityContext>() {
            @Override
            public void handle(SecurityContext sc) {
                assertNotNull(sc);
                assertNull(sc.getUserPrincipal());
                assertFalse(sc.isUserInRole("1"));
                assertFalse(sc.isSecure());
                assertNull(sc.getAuthenticationScheme());
                done[0] = true;
            }
        });

        assert (done[0]);

    }

    @Test
    public void testGetSecurityContext_Secure() throws Exception {

        DefaultSecurityContextProvider provider = new DefaultSecurityContextProvider();
        HttpServerRequest request = mock(HttpServerRequest.class);
        URI uri = URI.create("https://test.englishtown.com/test");
        when(request.absoluteURI()).thenReturn(uri);
        final boolean[] done = {false};

        provider.getSecurityContext(request, new Handler<SecurityContext>() {
            @Override
            public void handle(SecurityContext sc) {
                assertNotNull(sc);
                assertNull(sc.getUserPrincipal());
                assertFalse(sc.isUserInRole("1"));
                assertTrue(sc.isSecure());
                assertNull(sc.getAuthenticationScheme());
                done[0] = true;
            }
        });

        assert (done[0]);

    }
}
