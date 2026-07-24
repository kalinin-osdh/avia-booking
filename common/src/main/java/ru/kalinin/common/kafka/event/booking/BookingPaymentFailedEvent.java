package ru.kalinin.common.kafka.event.booking;

import ru.kalinin.common.kafka.event.EventMetaData;

import java.util.UUID;

public record BookingPaymentFailedEvent(
        EventMetaData metaData,
        UUID bookingNumber,
        String flightNumber,
        String seatNumber,
        String reason
) {
}
