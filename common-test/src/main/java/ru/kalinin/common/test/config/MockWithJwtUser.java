package ru.kalinin.common.test.config;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
@WithSecurityContext(
        factory = WithMockJwtUserSecurityContextFactory.class
)
public @interface MockWithJwtUser {
    long id() default 1L;
    String username() default "kalinin";
}
