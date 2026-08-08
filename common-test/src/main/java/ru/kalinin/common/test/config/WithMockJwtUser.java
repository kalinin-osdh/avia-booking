package ru.kalinin.common.test.config;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
@WithSecurityContext(
        factory = WithMockJwtUserSecurityContextFactory.class
)
public @interface WithMockJwtUser {
    long id() default 1L;
    String username() default "kalinin";
    String role() default "USER";
}
