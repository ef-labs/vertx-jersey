package com.englishtown.vertx.jersey.features.jackson.internal;

import com.englishtown.vertx.jersey.features.jackson.ObjectMapperConfigurator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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

    @Inject
    public ObjectMapperProvider(ObjectMapperConfigurator configurator) {

        mapper = new ObjectMapper();

        Module module = new SimpleModule("et.vertx.serializers", Version.unknownVersion())
                .addSerializer(JsonObject.class, new JsonObjectSerializer())
                .addSerializer(JsonArray.class, new JsonArraySerializer())
                .addDeserializer(JsonObject.class, new JsonObjectDeserializer())
                .addDeserializer(JsonArray.class, new JsonArrayDeserializer());

        mapper.registerModule(module);
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
