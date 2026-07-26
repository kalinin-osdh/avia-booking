package ru.kalinin.common.exception.refresh_token;

import ru.kalinin.common.exception.model.NotFoundException;

public class RefreshTokenNotFoundException extends NotFoundException {
    public RefreshTokenNotFoundException(String token) {
        super("Refresh token не найден: " + token);
    }
}
