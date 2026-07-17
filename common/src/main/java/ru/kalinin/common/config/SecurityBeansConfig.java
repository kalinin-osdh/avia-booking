package ru.kalinin.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.kalinin.common.security.filter.JwtAuthFilter;
import ru.kalinin.common.security.service.JwtService;
import ru.kalinin.common.security.handler.CustomAccessDeniedHandler;
import ru.kalinin.common.security.handler.CustomAuthenticationEntryPoint;

@Configuration
public class SecurityBeansConfig {

    @Bean
    public JwtService jwtService(){
        return new JwtService();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(){
        return new JwtAuthFilter(jwtService());
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public CustomAuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint(objectMapper());
    }

    @Bean
    public CustomAccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(objectMapper());
    }
}
