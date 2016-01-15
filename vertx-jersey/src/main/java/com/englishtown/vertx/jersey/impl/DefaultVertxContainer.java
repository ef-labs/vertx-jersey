package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.ApplicationConfigurator;
import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.VertxContainer;
import com.englishtown.vertx.jersey.inject.InternalVertxJerseyBinder;
import com.englishtown.vertx.jersey.inject.Nullable;
import io.vertx.core.Vertx;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.jvnet.hk2.annotations.Optional;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Vert.x implementation of {@link Container}
 */
public class DefaultVertxContainer implements VertxContainer {

    private final Vertx vertx;
    private final ServiceLocator locator;
    private final ApplicationConfigurator configurator;
    private JerseyOptions options;
    private ApplicationHandlerDelegate applicationHandlerDelegate;
    private boolean started;

    @Inject
    public DefaultVertxContainer(Vertx vertx, JerseyOptions options, @Optional @Nullable ServiceLocator locator, @Optional @Nullable ApplicationConfigurator configurator) {
        this.vertx = vertx;
        this.options = options;
        this.locator = locator;
        this.configurator = configurator;
    }

    /**
     * Starts the container
     */
    @Override
    public void start() {
        if (started) {
            return;
        }
        ApplicationHandler handler = getApplicationHandler();
        if (handler == null) {
            throw new IllegalStateException("ApplicationHandler cannot be null");
        }
        handler.onStartup(this);
        started = true;
    }

    /**
     * Stops the container
     */
    @Override
    public void stop() {
        if (!started) {
            return;
        }
        getApplicationHandler().onShutdown(this);
        applicationHandlerDelegate = null;
        started = false;
    }

    /**
     * Returns the current vertx instance
     *
     * @return the {@link Vertx} instance
     */
    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public JerseyOptions getOptions() {
        return options;
    }

    /**
     * Set the jersey configuration options
     *
     * @param options
     * @return
     */
    @Override
    public VertxContainer setOptions(JerseyOptions options) {
        this.options = options;
        return this;
    }

    @Override
    public ApplicationHandlerDelegate getApplicationHandlerDelegate() {
        if (applicationHandlerDelegate == null) {
            ResourceConfig rc = createConfiguration();
            ApplicationHandler applicationHandler = new ApplicationHandler(rc, null, locator);
            applicationHandlerDelegate = new DefaultApplicationHandlerDelegate(applicationHandler);
        }
        return applicationHandlerDelegate;
    }

    /**
     * Return an immutable representation of the current {@link ResourceConfig
     * configuration}.
     *
     * @return current configuration of the hosted Jersey application.
     */
    @Override
    public ResourceConfig getConfiguration() {
        ApplicationHandler handler = getApplicationHandler();
        return handler == null ? null : handler.getConfiguration();
    }

    /**
     * Get the Jersey server-side application handler associated with the container.
     *
     * @return Jersey server-side application handler associated with the container.
     */
    @Override
    public ApplicationHandler getApplicationHandler() {
        return getApplicationHandlerDelegate().getApplicationHandler();
    }

    /**
     * Reload the hosted Jersey application using the current {@link ResourceConfig
     * configuration}.
     */
    @Override
    public void reload() {
        reload(getConfiguration());
    }

    /**
     * Reload the hosted Jersey application using a new {@link ResourceConfig
     * configuration}.
     *
     * @param configuration new configuration used for the reload.
     */
    @Override
    public void reload(ResourceConfig configuration) {
        ApplicationHandler applicationHandler = new ApplicationHandler(configuration, null, locator);
        applicationHandlerDelegate = new DefaultApplicationHandlerDelegate(applicationHandler);
        getApplicationHandler().onReload(this);
        applicationHandler.onStartup(this);
    }

    protected ResourceConfig createConfiguration() {

        ResourceConfig rc = new ResourceConfig();

        List<String> packages = options.getPackages();
        Set<Class<?>> components = options.getComponents();
        boolean hasPackages = (packages != null && !packages.isEmpty());
        boolean hasComponents = (components != null && !components.isEmpty());

        if (!hasComponents && !hasPackages) {
            throw new IllegalStateException("At least one resource package name or component must be specified");
        }

        if (hasPackages) {
            rc.packages(packages.toArray(new String[packages.size()]));
        }
        if (hasComponents) {
            rc.registerClasses(components);
        }

        // Always register the InternalVertxJerseyBinder
        rc.register(new InternalVertxJerseyBinder(vertx));

        // Register configured binders
        Set<Object> instances = options.getInstances();
        if (instances != null) {
            rc.registerInstances(instances);
        }

        Map<String, Object> properties = options.getProperties();
        if (properties != null) {
            rc.addProperties(properties);
        }

        if (configurator != null) {
            rc = configurator.configure(rc);
        }

        return rc;
    }

}
