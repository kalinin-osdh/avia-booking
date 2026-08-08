package ru.kalinin.booking.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import ru.kalinin.common.security.handler.CustomAccessDeniedHandler;
import ru.kalinin.common.security.handler.CustomAuthenticationEntryPoint;
import ru.kalinin.common.test.config.TestSecurityConfig;

@TestConfiguration
public class BookingTestSecurityConfig extends TestSecurityConfig {
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http,
                                                       CustomAuthenticationEntryPoint authenticationEntryPoint,
                                                       CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
        configureCommon(http, authenticationEntryPoint, accessDeniedHandler);
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/booking/**").authenticated()
                );
        return http.build();
    }
}
