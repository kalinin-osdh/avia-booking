package ru.kalinin.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kalinin.auth.dto.mapper.UserMapper;
import ru.kalinin.auth.dto.request.AuthRequest;
import ru.kalinin.auth.dto.request.RefreshRequest;
import ru.kalinin.auth.dto.response.AuthResponse;
import ru.kalinin.auth.entity.User;
import ru.kalinin.auth.entity.enums.Role;
import ru.kalinin.auth.repository.UserRepository;
import ru.kalinin.auth.security.CustomUserDetails;
import ru.kalinin.auth.security.JwtTokenService;
import ru.kalinin.auth.service.impl.AuthServiceImpl;
import ru.kalinin.auth.service.interfaces.RefreshTokenService;
import ru.kalinin.common.exception.refresh_token.RefreshTokenNotValid;
import ru.kalinin.common.exception.users.UserExistsException;
import ru.kalinin.common.exception.users.UserNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("auth-service")
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private static AuthRequest request;
    private static AuthResponse response;
    private static User user;
    private static CustomUserDetails userDetails;

    @BeforeEach
    void setUpTestData() {
        request = new AuthRequest(
                "kalinin",
                "kalinin13"
        );

        response = new AuthResponse(
                "kalinin",
                "someAccessToken",
                "someRefreshToken"
        );

        user = new User(
                1L,
                "kalinin",
                "hashPassword",
                Role.USER
        );

        userDetails = new CustomUserDetails(
                1L,
                "kalinin",
                "hashPassword",
                Collections.emptyList()
        );
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void shouldRegister() {
        when(userRepository.existsByUsername("kalinin")).thenReturn(false);
        when(passwordEncoder.encode("kalinin13")).thenReturn("hashPassword");
        when(userMapper.toEntity(any(AuthRequest.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDetails(any(User.class))).thenReturn(userDetails);
        when(jwtTokenService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("someAccessToken");
        when(refreshTokenService.generateRefreshToken(1L)).thenReturn("someRefreshToken");
        when(userMapper.toAuthResponse(
                user.getUsername(),
                "someAccessToken",
                "someRefreshToken")
        ).thenReturn(response);

        AuthResponse authResponse = authService.register(request);

        assertEquals("someAccessToken", authResponse.getAccessToken());
        assertEquals("someRefreshToken", authResponse.getRefreshToken());

        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtTokenService, times(1)).generateAccessToken(any(CustomUserDetails.class));
        verify(refreshTokenService, times(1)).generateRefreshToken(1L);
    }

    @Test
    @DisplayName("Ошибка при регистрации - такой пользователь уже существует")
    void shouldThrowUserExistsExceptionWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("kalinin")).thenReturn(true);

        assertThrows(
                UserExistsException.class,
                () -> authService.register(request)
        );

        verify(userRepository, times(1)).existsByUsername("kalinin");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(jwtTokenService, refreshTokenService);
    }

    @Test
    @DisplayName("Успешная аутентификация пользователя")
    void shouldLogin() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(any());

        when(userRepository.findByUsername("kalinin")).thenReturn(Optional.of(user));

        when(userMapper.toUserDetails(any(User.class))).thenReturn(userDetails);
        when(jwtTokenService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("someAccessToken");
        when(refreshTokenService.generateRefreshToken(1L)).thenReturn("someRefreshToken");
        when(userMapper.toAuthResponse(
                user.getUsername(),
                "someAccessToken",
                "someRefreshToken")
        ).thenReturn(response);

        AuthResponse authResponse = authService.login(request);

        assertEquals("someAccessToken", authResponse.getAccessToken());
        assertEquals("someRefreshToken", authResponse.getRefreshToken());

        verify(userRepository, times(1)).findByUsername("kalinin");
        verify(jwtTokenService, times(1)).generateAccessToken(any(CustomUserDetails.class));
        verify(refreshTokenService, times(1)).generateRefreshToken(1L);
    }

    @Test
    @DisplayName("Ошибка при аутентификации - пользователь не найден по имени")
    void shouldThrowUsernameNotFoundExceptionWhenLogin() {
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(
                        new UsernamePasswordAuthenticationToken(
                                "kalinin",
                                "kalinin13",
                                Collections.emptyList()
                        )
                );

        when(userRepository.findByUsername("kalinin")).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> authService.login(request)
        );

        verify(userRepository, times(1)).findByUsername("kalinin");
        verifyNoInteractions(jwtTokenService, refreshTokenService);
    }

    @Test
    @DisplayName("Успешное получение нового рефреш токена")
    void shouldRefresh() {
        when(refreshTokenService.isRefreshTokenValid("refreshToken")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserDetails(any(User.class))).thenReturn(userDetails);
        when(jwtTokenService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("someAccessToken");
        when(refreshTokenService.generateRefreshToken(1L)).thenReturn("someRefreshToken");
        when(userMapper.toAuthResponse(
                user.getUsername(),
                "someAccessToken",
                "someRefreshToken")
        ).thenReturn(response);

        AuthResponse authResponse = authService.refresh(new RefreshRequest("refreshToken"));

        assertEquals("someAccessToken", authResponse.getAccessToken());
        assertEquals("someRefreshToken", authResponse.getRefreshToken());

        verify(userRepository, times(1)).findById(1L);
        verify(jwtTokenService, times(1)).generateAccessToken(any(CustomUserDetails.class));
        verify(refreshTokenService, times(1)).isRefreshTokenValid("refreshToken");
        verify(refreshTokenService, times(1)).generateRefreshToken(1L);
    }

    @Test
    @DisplayName("Ошибка получения нового рефреш токена - рефреш токен не валидный")
    void shouldThrowExceptionWhenRefreshTokenIsNotValid() {
        when(refreshTokenService.isRefreshTokenValid("refreshToken")).thenReturn(-1L);

        assertThrows(
                RefreshTokenNotValid.class,
                () -> authService.refresh(new RefreshRequest("refreshToken"))
        );

        verify(refreshTokenService, times(1)).isRefreshTokenValid("refreshToken");
        verify(refreshTokenService, never()).generateRefreshToken(1L);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    @DisplayName("Успешный выход из сервиса")
    void shouldLogout() {
        when(userRepository.findByUsername("kalinin")).thenReturn(Optional.of(user));

        authService.logout("kalinin");

        verify(userRepository, times(1)).findByUsername("kalinin");
        verify(refreshTokenService, times(1)).deleteRefreshToken(1L);

    }

    @Test
    @DisplayName("Успешный выход из сервиса")
    void shouldThrowUserNotFoundExceptionWhenLogout() {
        when(userRepository.findByUsername("kalinin")).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> authService.logout("kalinin")
        );

        verify(userRepository, times(1)).findByUsername("kalinin");
        verifyNoInteractions(refreshTokenService);
    }
}
