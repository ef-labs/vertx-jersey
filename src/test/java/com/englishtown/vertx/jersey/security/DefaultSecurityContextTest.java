package com.englishtown.vertx.jersey.security;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/18/13
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultSecurityContextTest {

    @Test
    public void testGetSecurityContext() throws Exception {

        boolean isSecure = false;
        DefaultSecurityContext securityContext = new DefaultSecurityContext(isSecure);

        assertNull(securityContext.getUserPrincipal());
        assertFalse(securityContext.isUserInRole("1"));
        assertFalse(securityContext.isSecure());
        assertNull(securityContext.getAuthenticationScheme());

    }

    @Test
    public void testGetSecurityContext_Secure() throws Exception {

        boolean isSecure = true;
        DefaultSecurityContext securityContext = new DefaultSecurityContext(isSecure);

        assertNull(securityContext.getUserPrincipal());
        assertFalse(securityContext.isUserInRole("1"));
        assertTrue(securityContext.isSecure());
        assertNull(securityContext.getAuthenticationScheme());

    }
}
