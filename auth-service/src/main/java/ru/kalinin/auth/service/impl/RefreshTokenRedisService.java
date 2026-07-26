package ru.kalinin.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.kalinin.auth.service.interfaces.RefreshTokenService;
import ru.kalinin.common.exception.refresh_token.RefreshTokenNotFoundException;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService implements RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKEN_PREFIX = "user_token:";
    private static final int TOKEN_LENGTH = 32;

    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration;

    @Override
    public String generateRefreshToken(Long userId) {
        String token = generateToken();

        rotateRefreshToken(userId ,token);

        return token;
    }

    @Override
    public void rotateRefreshToken(Long userId, String token){
        deleteRefreshToken(userId);
        saveRefreshToken(userId,token);
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        String userTokenKey = USER_TOKEN_PREFIX + userId;
        String oldToken = redisTemplate.opsForValue().get(userTokenKey);
        if (oldToken != null) {
            String refreshTokenKey = REFRESH_TOKEN_PREFIX + oldToken;
            redisTemplate.delete(List.of(refreshTokenKey, userTokenKey));
        }
    }

    @Override
    public Long isRefreshTokenValid(String token) {
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(refreshTokenKey);
        if(userId==null){
            return -1L;
        }
        return Long.valueOf(userId);
    }

    @Override
    public void saveRefreshToken(Long userId, String token) {
        Duration TTL = Duration.ofMillis(refreshTokenExpiration);

        String refreshTokenKey = REFRESH_TOKEN_PREFIX + token;
        String userTokenKey = USER_TOKEN_PREFIX + userId;

        redisTemplate.opsForValue().set(refreshTokenKey, userId.toString(), TTL);
        redisTemplate.opsForValue().set(userTokenKey, token, TTL);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
