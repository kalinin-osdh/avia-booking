package ru.kalinin.common.security.dto;

public record JwtUserPrincipal(
        Long id,
        String username
) {
}
