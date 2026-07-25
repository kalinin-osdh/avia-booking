package ru.kalinin.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.kalinin.notification.dto.TelegramProperties;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramService {
    private final TelegramProperties properties;

    private final RestClient restClient = RestClient.create();

    public void sendMessage(String message){
        restClient.post()
                .uri("https://api.telegram.org/bot{token}/sendMessage",
                        properties.token())
                .body(Map.of(
                        "chat_id", properties.chatId(),
                        "text", message
                ))
                .retrieve()
                .toBodilessEntity();
    }

}
