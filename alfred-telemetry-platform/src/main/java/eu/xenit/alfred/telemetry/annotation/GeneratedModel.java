package eu.xenit.alfred.telemetry.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Excludes annotated class from code coverage
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface GeneratedModel {}