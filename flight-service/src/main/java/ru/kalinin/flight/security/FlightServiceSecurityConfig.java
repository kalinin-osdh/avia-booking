package ru.kalinin.flight.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import ru.kalinin.common.config.BaseSecurityConfig;
import ru.kalinin.common.security.filter.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class FlightServiceSecurityConfig extends BaseSecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureCommon(http, jwtAuthFilter, authenticationEntryPoint, accessDeniedHandler);
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
