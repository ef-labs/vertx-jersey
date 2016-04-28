package com.englishtown.vertx.jersey.features.jackson.internal;

import com.englishtown.vertx.jersey.features.jackson.ObjectMapperConfigurator;
import com.englishtown.vertx.jersey.features.jackson.VertxSerializerModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides a Jackson {@link ObjectMapper} to Jersey
 */
@Provider
@Singleton
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    /**
     * @param configurator
     * @deprecated Use overload with injected {@link ObjectMapper}
     */
    @Deprecated
    public ObjectMapperProvider(ObjectMapperConfigurator configurator) {
        this(configurator, new ObjectMapper());
    }

    /**
     * DI constructor
     *
     * @param configurator
     * @param mapper
     */
    @Inject
    public ObjectMapperProvider(ObjectMapperConfigurator configurator, ObjectMapper mapper) {
        this.mapper = mapper;

        mapper.registerModule(new VertxSerializerModule());
        configurator.configure(mapper);
    }

    /**
     * Get a context of type {@code T} that is applicable to the supplied
     * type.
     *
     * @param type the class of object for which a context is desired
     * @return a context for the supplied type or {@code null} if a
     * context for the supplied type is not available from this provider.
     */
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

}
