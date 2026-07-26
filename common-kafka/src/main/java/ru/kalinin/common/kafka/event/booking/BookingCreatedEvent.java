package ru.kalinin.common.kafka.event.booking;

import ru.kalinin.common.kafka.event.EventMetadata;

import java.util.UUID;

public record BookingCreatedEvent(
        EventMetadata metadata,
        Long bookingId,
        UUID bookingNumber,
        String username,
        String flightNumber,
        String seatNumber
) {
    public static BookingCreatedEvent of(
            Long bookingId,
            UUID bookingNumber,
            String username,
            String flightNumber,
            String seatNumber
    ) {
        return new BookingCreatedEvent(
                EventMetadata.create(),
                bookingId,
                bookingNumber,
                username,
                flightNumber,
                seatNumber
        );
    }
}
