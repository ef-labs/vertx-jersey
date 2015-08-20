package com.englishtown.vertx.features.swagger.internal;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Minimum {@link ServletContext} implementation for swagger
 */
class SwaggerServletContext implements ServletContext {

    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContextPath() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletContext getContext(String uripath) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMajorVersion() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinorVersion() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMimeType(String file) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getResourcePaths(String path) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResource(String path) throws MalformedURLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResourceAsStream(String path) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Servlet getServlet(String name) throws ServletException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<Servlet> getServlets() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getServletNames() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(String msg) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(Exception exception, String msg) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(String message, Throwable throwable) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRealPath(String path) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerInfo() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInitParameter(String name) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletContextName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(String className) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void declareRoles(String... roleNames) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVirtualServerName() {
        return null;
    }
}
