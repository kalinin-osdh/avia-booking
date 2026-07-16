package ru.kalinin.common.exception.user;

import ru.kalinin.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long id) {
        super("Пользователь не найден: " + id);
    }

    public UserNotFoundException(String username) {
        super("Пользователь не найден: " + username);
    }
}
