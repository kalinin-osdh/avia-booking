package ru.kalinin.common.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventMetadata(
        UUID eventId,
        LocalDateTime createdAt
) {
    public static EventMetadata create() {
        return new EventMetadata(
                UUID.randomUUID(),
                LocalDateTime.now()
        );
    }
}
