package ru.kalinin.common.kafka.event.payment;

import ru.kalinin.common.kafka.event.EventMetadata;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentSuccessfulEvent(
        EventMetadata metadata,
        UUID bookingNumber,
        UUID paymentNumber,
        String username,
        BigDecimal price
) {
    public static PaymentSuccessfulEvent of(
            UUID bookingNumber,
            UUID paymentNumber,
            String username,
            BigDecimal price
    ) {
        return new PaymentSuccessfulEvent(
                EventMetadata.create(),
                bookingNumber,
                paymentNumber,
                username,
                price
        );
    }
}
