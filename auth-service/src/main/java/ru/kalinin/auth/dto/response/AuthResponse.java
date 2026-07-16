package ru.kalinin.auth.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
public class AuthResponse {
    String username;
    String accessToken;
    String refreshToken;
}
