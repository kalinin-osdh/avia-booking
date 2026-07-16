package ru.kalinin.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {
    @NotBlank(message = "Введите имя пользователя")
    @Size(min = 5, max = 20, message = "Имя пользователя должно быть от 5 до 20 символов")
    String username;
    @NotBlank(message = "Введите пароль")
    @Size(min = 8, max = 255, message = "Длина пароля минимум 8 символов")
    String password;
}
