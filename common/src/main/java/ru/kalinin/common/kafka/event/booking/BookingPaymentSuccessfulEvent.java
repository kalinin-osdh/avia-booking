package ru.kalinin.common.kafka.event.booking;

import ru.kalinin.common.kafka.event.EventMetadata;

import java.util.UUID;

public record BookingPaymentSuccessfulEvent(
        EventMetadata metadata,
        UUID bookingNumber,
        String flightNumber,
        String seatNumber
) {
    public static BookingPaymentSuccessfulEvent of(
            UUID bookingNumber,
            String flightNumber,
            String seatNumber
    ) {
        return new BookingPaymentSuccessfulEvent(
                EventMetadata.create(),
                bookingNumber,
                flightNumber,
                seatNumber
        );
    }
}
