package com.englishtown.vertx.jersey.features.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configure the Jackson ObjectMapper
 */
public interface ObjectMapperConfigurator {

    void configure(ObjectMapper mapper);

}
