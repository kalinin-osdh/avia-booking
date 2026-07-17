package ru.kalinin.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class JwtUserPrincipal {
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;
}
