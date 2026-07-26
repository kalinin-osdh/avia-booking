package ru.kalinin.common.exception.model;

import lombok.Value;

@Value
public class ErrorResponse {
    String error;
    String message;
}
