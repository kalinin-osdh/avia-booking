package ru.kalinin.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import ru.kalinin.auth.service.impl.RefreshTokenServiceImpl;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("refresh-service")
public class RefreshTokenServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    @DisplayName("Удаление старого и сохранение нового рефреш токена")
    void shouldRotateRefreshToken() {
        RefreshTokenServiceImpl spyService =
                spy(refreshTokenService);

        doNothing().when(spyService).deleteRefreshToken(1L);
        doNothing().when(spyService).saveRefreshToken(1L, "token");

        spyService.rotateRefreshToken(1L, "token");

        InOrder order = inOrder(spyService);

        order.verify(spyService).deleteRefreshToken(1L);
        order.verify(spyService).saveRefreshToken(1L, "token");
    }

    @Test
    @DisplayName("Возвращение [Long userId] когда рефреш токен валидный")
    void shouldReturnUserIdWhenRefreshTokenValid() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get("refresh_token:token")).thenReturn("1");

        Long result = refreshTokenService.isRefreshTokenValid("token");

        assertEquals(1L, result);

        verify(valueOperations).get("refresh_token:token");
    }

    @Test
    @DisplayName("Успешное сохранение рефреш токена")
    void shouldSaveRefreshToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        refreshTokenService.saveRefreshToken(1L, "token");

        verify(valueOperations)
                .set(
                        eq("refresh_token:token"),
                        eq("1"),
                        any(Duration.class));
        verify(valueOperations)
                .set(
                        eq("user_token:1"),
                        eq("token"),
                        any(Duration.class));
    }

    @Test
    @DisplayName("Успешное удаление рефреш токена")
    void shouldDeleteRefreshToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get("user_token:1")).thenReturn("oldToken");

        refreshTokenService.deleteRefreshToken(1L);

        verify(redisTemplate)
                .delete(
                        List.of(
                                "refresh_token:oldToken",
                                "user_token:1"
                        )
                );
    }

}
