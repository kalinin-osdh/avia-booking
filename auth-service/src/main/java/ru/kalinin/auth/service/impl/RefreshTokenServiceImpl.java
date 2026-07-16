package ru.kalinin.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.auth.entity.RefreshToken;
import ru.kalinin.auth.entity.User;
import ru.kalinin.auth.repository.RefreshTokenRepository;
import ru.kalinin.auth.service.interfaces.RefreshTokenService;
import ru.kalinin.common.exception.refresh_token.RefreshTokenNotFoundException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;

    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration;

    @Override
    public String generateRefreshToken(User user) {
        rotateRefreshToken(user);

        String token = generateToken();

        saveRefreshToken(user, token);

        return token;
    }

    @Override
    public void rotateRefreshToken(User user) {
        Set<RefreshToken> validRefreshTokens = user.getRefreshTokens().stream()
                .filter(p -> !p.isRevoked())
                .collect(Collectors.toSet());

        if (!validRefreshTokens.isEmpty()){
            validRefreshTokens.forEach(obj->obj.setRevoked(true));
        }
    }

    @Override
    public boolean isRefreshTokenValid(String token) {
        RefreshToken refreshToken = repository.findByToken(token).orElseThrow(
                () -> new RefreshTokenNotFoundException(token)
        );

        return !isTokenExpired(refreshToken.getExpiresAt()) && !refreshToken.isRevoked();
    }

    private boolean isTokenExpired(LocalDateTime localDateTime) {
        return localDateTime.isBefore(LocalDateTime.now());
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration))
                .revoked(false)
                .build();

        repository.save(refreshToken);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
