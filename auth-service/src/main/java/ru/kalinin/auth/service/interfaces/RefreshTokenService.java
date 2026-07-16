package ru.kalinin.auth.service.interfaces;

import ru.kalinin.auth.entity.User;

public interface RefreshTokenService {
    String generateRefreshToken(User user);

    void rotateRefreshToken(User user);

    boolean isRefreshTokenValid(String token);
}
