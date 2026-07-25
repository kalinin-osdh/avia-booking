package ru.kalinin.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramService {
    @Value("${telegram.token}")
    private String token;

    @Value("${telegram.chat-id}")
    private Long chatId;

    private final RestClient restClient = RestClient.create();

    public void sendMessage(String message) {
        restClient.post()
                .uri("https://api.telegram.org/bot{token}/sendMessage",
                        token)
                .body(Map.of(
                        "chat_id", chatId,
                        "text", message
                ))
                .retrieve()
                .toBodilessEntity();
    }

}
