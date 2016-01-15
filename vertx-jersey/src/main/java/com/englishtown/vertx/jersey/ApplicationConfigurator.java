package com.englishtown.vertx.jersey;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Injectable interface to modify the application's {@link ResourceConfig}
 */
public interface ApplicationConfigurator {

    ResourceConfig configure(ResourceConfig rc);

}
