package ru.kalinin.common.exception.refresh_token;

import ru.kalinin.common.exception.model.CustomException;

public class RefreshTokenNotValid extends CustomException {

    public RefreshTokenNotValid() {
        super("Нужно провести повторную аутентификацию пользователя");
    }
}
