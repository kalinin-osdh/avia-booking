package ru.kalinin.flight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import ru.kalinin.flight.dto.response.PageResponse;
import ru.kalinin.flight.dto.response.FlightWithSeatsResponse;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    private final RedisConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;

    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheConfiguration configuration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(5))
                        .disableCachingNullValues();


        RedisCacheConfiguration flightByNumberConfig =
                configuration.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(
                                        objectMapper,
                                        FlightWithSeatsResponse.class
                                )
                        )
                );

        RedisCacheConfiguration flightPage =
                configuration.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(
                                        objectMapper,
                                        PageResponse.class
                                )
                        )
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(configuration)
                .withCacheConfiguration(
                        "flightByNumber",
                        flightByNumberConfig
                )
                .withCacheConfiguration(
                        "flightPage",
                        flightPage
                )
                .build();
    }
}
