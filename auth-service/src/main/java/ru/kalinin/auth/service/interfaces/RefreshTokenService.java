package ru.kalinin.auth.service.interfaces;

public interface RefreshTokenService {
    String generateRefreshToken(Long userId);

    void saveRefreshToken(Long userId, String token);

    void deleteRefreshToken(Long userId);

    void rotateRefreshToken(Long userId, String token);

    Long isRefreshTokenValid(String token);
}
