package com.team44.isa_youtubeich.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to apply rate limiting to controller methods.
 * Limits the number of requests that can be made from a single IP address
 * within a specified time window.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * Unique key to identify this rate limit bucket.
     * Different keys will have separate rate limit counters.
     * If not specified, uses the full method signature.
     */
    String key() default "";

    /**
     * Maximum number of requests allowed within the time window
     */
    int limit() default 5;

    /**
     * Time window in seconds
     */
    int windowSeconds() default 60;
}
