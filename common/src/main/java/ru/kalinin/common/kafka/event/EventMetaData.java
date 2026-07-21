package ru.kalinin.common.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventMetaData(
        UUID eventId,
        LocalDateTime createdAt
) {
}
