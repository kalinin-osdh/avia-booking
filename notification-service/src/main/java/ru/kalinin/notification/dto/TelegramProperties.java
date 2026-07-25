package ru.kalinin.notification.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties(
        String token,
        Long chatId
) {
}
