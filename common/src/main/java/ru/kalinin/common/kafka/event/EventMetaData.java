package ru.kalinin.common.kafka.dto.booking;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventMetaData(
        UUID eventId,
        LocalDateTime createdAt,
        String EventType
) {
}
