package ru.kalinin.common.kafka.event.payment;

import ru.kalinin.common.kafka.event.EventMetadata;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentFailedEvent(
        EventMetadata metadata,
        UUID bookingNumber,
        UUID paymentNumber,
        String username,
        BigDecimal price,
        String reason
) {
    public static PaymentFailedEvent of(
            UUID bookingNumber,
            UUID paymentNumber,
            String username,
            BigDecimal price,
            String reason
    ) {
        return new PaymentFailedEvent(
                EventMetadata.create(),
                bookingNumber,
                paymentNumber,
                username,
                price,
                reason
        );
    }
}
