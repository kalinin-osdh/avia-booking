package ru.kalinin.common.test.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.kalinin.common.security.dto.JwtUserPrincipal;

import java.util.Collections;

public class WithMockJwtUserSecurityContextFactory implements WithSecurityContextFactory<MockWithJwtUser> {

    @Override
    public SecurityContext createSecurityContext(MockWithJwtUser annotation) {
        JwtUserPrincipal principal = new JwtUserPrincipal(
                annotation.id(),
                annotation.username()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.emptyList()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        return context;
    }
}
