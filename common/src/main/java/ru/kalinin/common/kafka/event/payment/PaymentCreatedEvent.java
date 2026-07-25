package ru.kalinin.common.kafka.event.payment;

import ru.kalinin.common.kafka.event.EventMetadata;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCreatedEvent(
        EventMetadata metadata,
        UUID bookingNumber,
        String username,
        BigDecimal price
) {
    public static PaymentCreatedEvent of(
            UUID bookingNumber,
            String username,
            BigDecimal price
    ) {
        return new PaymentCreatedEvent(
                EventMetadata.create(),
                bookingNumber,
                username,
                price
        );
    }
}
