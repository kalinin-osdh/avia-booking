package ru.kalinin.common.exception.users;

import ru.kalinin.common.exception.CustomException;

public class UserExistsException extends CustomException {

    public UserExistsException(String username) {
        super("Пользователь с таким именем уже существует: " + username);
    }
}

