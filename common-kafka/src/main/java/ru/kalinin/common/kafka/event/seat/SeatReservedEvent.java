package ru.kalinin.common.kafka.event.seat;

import ru.kalinin.common.kafka.event.EventMetadata;

import java.math.BigDecimal;
import java.util.UUID;

public record SeatReservedEvent(
        EventMetadata metadata,
        Long bookingId,
        UUID bookingNumber,
        String username,
        String flightNumber,
        String seatNumber,
        BigDecimal price
) {
    public static SeatReservedEvent of(
            Long bookingId,
            UUID bookingNumber,
            String username,
            String flightNumber,
            String seatNumber,
            BigDecimal price
    ) {
        return new SeatReservedEvent(
                EventMetadata.create(),
                bookingId,
                bookingNumber,
                username,
                flightNumber,
                seatNumber,
                price
        );
    }
}
