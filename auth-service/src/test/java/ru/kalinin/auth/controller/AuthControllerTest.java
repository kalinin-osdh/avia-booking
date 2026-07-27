package ru.kalinin.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.kalinin.auth.dto.request.AuthRequest;
import ru.kalinin.auth.dto.request.RefreshRequest;
import ru.kalinin.auth.dto.response.AuthResponse;
import ru.kalinin.auth.service.interfaces.AuthService;
import ru.kalinin.common.security.dto.JwtUserPrincipal;
import ru.kalinin.common.test.config.MockWithJwtUser;
import ru.kalinin.common.test.config.TestSecurityConfig;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private static AuthRequest request;
    private static AuthResponse response;

    @BeforeAll
    static void setUpTestData() {
        request = new AuthRequest(
                "kalinin",
                "kalinin13"
        );

        response = new AuthResponse(
                "kalinin",
                "someAccessToken",
                "someRefreshToken"
        );
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - 201 Успешная регистрация")
    void shouldRegister() throws Exception {
        when(authService.register(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken()));

        verify(authService).register(any(AuthRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 200 Успешная аутентификация")
    void shouldLogin() throws Exception {
        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken()));

        verify(authService).login(any(AuthRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 401 Неверные данные при аутентификации")
    void shouldBadRequestLogin() throws Exception {
        when(authService.login(any(AuthRequest.class))).thenThrow(
                new BadCredentialsException("BadCredentials")
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Неверный логин или пароль"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - 201 Получение нового refresh token")
    void shouldRefreshToken() throws Exception {
        RefreshRequest refreshRequest = new RefreshRequest(
                "oldRefreshToken"
        );

        when(authService.refresh(any(RefreshRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken()));

        verify(authService).refresh(any(RefreshRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - 200 Успешный выход из аккаунта")
    @MockWithJwtUser(username = "admin")
    void shouldLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());

        verify(authService, times(1)).logout("admin");
    }
}
