package ru.kalinin.common.dto;

import lombok.Value;

@Value
public class ErrorResponse {
    String error;
    String message;
}
