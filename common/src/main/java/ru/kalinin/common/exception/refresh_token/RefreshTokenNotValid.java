package ru.kalinin.common.exception;

public class RefreshTokenNotValid extends RuntimeException {

    public RefreshTokenNotValid(String token) {
        super("Refresh token is not valid: " + token + "\n"
                + "Нужно провести повторную аутентификацию пользователя");
    }
}
