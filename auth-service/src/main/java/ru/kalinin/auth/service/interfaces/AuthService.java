package ru.kalinin.auth.service.interfaces;

import org.springframework.security.core.userdetails.UserDetails;
import ru.kalinin.auth.dto.request.AuthRequest;
import ru.kalinin.auth.dto.request.RefreshRequest;
import ru.kalinin.auth.dto.response.AuthResponse;
import ru.kalinin.auth.entity.User;

public interface AuthService {
    AuthResponse register(AuthRequest request);

    AuthResponse login(AuthRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(String username);
}
