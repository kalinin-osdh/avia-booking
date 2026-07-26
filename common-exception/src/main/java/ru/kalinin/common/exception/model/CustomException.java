package ru.kalinin.common.exception.model;

public abstract class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
