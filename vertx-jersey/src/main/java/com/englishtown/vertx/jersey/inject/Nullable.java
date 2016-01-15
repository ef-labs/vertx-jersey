package com.englishtown.vertx.jersey.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Annotation to mark injected parameters and fields as nullable
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, PARAMETER})
public @interface Nullable {
}
