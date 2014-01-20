/*
 * The MIT License (MIT)
 * Copyright © 2013 Englishtown <opensource@englishtown.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.englishtown.vertx.jersey.security;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * Default implementation of {@link SecurityContext}
 */
public class DefaultSecurityContext implements SecurityContext {

    private final boolean isSecure;

    public DefaultSecurityContext(boolean isSecure) {
        this.isSecure = isSecure;
    }

    /**
     * Returns a <code>java.security.Principal</code> object containing the
     * name of the current authenticated user. If the user
     * has not been authenticated, the method returns null.
     *
     * @return a <code>java.security.Principal</code> containing the name
     * of the user making this request; null if the user has not been
     * authenticated
     * @throws IllegalStateException if called outside the scope of a request
     */
    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    /**
     * Returns a boolean indicating whether the authenticated user is included
     * in the specified logical "role". If the user has not been authenticated,
     * the method returns <code>false</code>.
     *
     * @param role a <code>String</code> specifying the name of the role
     * @return a <code>boolean</code> indicating whether the user making
     * the request belongs to a given role; <code>false</code> if the user
     * has not been authenticated
     * @throws IllegalStateException if called outside the scope of a request
     */
    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    /**
     * Returns a boolean indicating whether this request was made
     * using a secure channel, such as HTTPS.
     *
     * @return <code>true</code> if the request was made using a secure
     * channel, <code>false</code> otherwise
     * @throws IllegalStateException if called outside the scope of a request
     */
    @Override
    public boolean isSecure() {
        return isSecure;
    }

    /**
     * Returns the string value of the authentication scheme used to protect
     * the resource. If the resource is not authenticated, null is returned.
     * <p/>
     * Values are the same as the CGI variable AUTH_TYPE
     *
     * @return one of the static members BASIC_AUTH, FORM_AUTH,
     * CLIENT_CERT_AUTH, DIGEST_AUTH (suitable for == comparison) or the
     * container-specific string indicating the authentication scheme,
     * or null if the request was not authenticated.
     * @throws IllegalStateException if called outside the scope of a request
     */
    @Override
    public String getAuthenticationScheme() {
        return null;
    }
}
