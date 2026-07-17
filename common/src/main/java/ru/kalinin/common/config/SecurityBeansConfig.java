package ru.kalinin.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
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
