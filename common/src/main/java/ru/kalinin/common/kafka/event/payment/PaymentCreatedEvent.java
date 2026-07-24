package ru.kalinin.common.kafka.event.payment;

import ru.kalinin.common.kafka.event.EventMetaData;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCreatedEvent(
        EventMetaData metaData,
        UUID bookingNumber,
        String username,
        BigDecimal price
) {
}
