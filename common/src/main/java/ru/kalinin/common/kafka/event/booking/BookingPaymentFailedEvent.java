package ru.kalinin.common.kafka.event.booking;

import ru.kalinin.common.kafka.event.EventMetadata;

import java.util.UUID;

public record BookingPaymentFailedEvent(
        EventMetadata metadata,
        UUID bookingNumber,
        String flightNumber,
        String seatNumber,
        String reason
) {
    public static BookingPaymentFailedEvent of(
            UUID bookingNumber,
            String flightNumber,
            String seatNumber,
            String reason
    ) {
        return new BookingPaymentFailedEvent(
                EventMetadata.create(),
                bookingNumber,
                flightNumber,
                seatNumber,
                reason
        );
    }
}
