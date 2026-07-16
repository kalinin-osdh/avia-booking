package ru.kalinin.common.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.kalinin.common.dto.CustomException;

public class UserExistsException extends CustomException {

    public UserExistsException(String username) {
        super("Пользователь с таким именем уже существует: " + username);
    }
}

