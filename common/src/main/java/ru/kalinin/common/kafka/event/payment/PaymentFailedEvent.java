package ru.kalinin.common.kafka.event.payment;

import ru.kalinin.common.kafka.event.EventMetaData;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentFailedEvent(
        EventMetaData metaData,
        UUID bookingNumber,
        UUID paymentNumber,
        String username,
        BigDecimal price,
        String reason
) {
}
