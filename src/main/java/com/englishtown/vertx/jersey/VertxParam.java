package com.englishtown.vertx.jersey;

import java.lang.annotation.*;

/**
 * This annotation is used to inject Vertx parameters into a resource class field,
 * property or resource method parameter.
 * <<p>
 * Supported Vertx objects include: {@link org.vertx.java.core.Vertx}, {@link org.vertx.java.platform.Container},
 * {@link org.vertx.java.core.http.HttpServerRequest}, {@link org.vertx.java.core.http.impl.HttpReadStreamBase},
 * and {@link org.vertx.java.core.streams.ReadStream}
 * /p>
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VertxParam {
}
