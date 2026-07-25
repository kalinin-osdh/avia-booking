package ru.kalinin.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import ru.kalinin.notification.dto.TelegramProperties;

@SpringBootApplication
@ComponentScan(basePackages = {
        "ru.kalinin.notification",
        "ru.kalinin.common.kafka"
})
@EnableConfigurationProperties(TelegramProperties.class)
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
