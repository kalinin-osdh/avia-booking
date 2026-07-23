package ru.kalinin.common.kafka.event.seat;

import ru.kalinin.common.kafka.event.EventMetaData;

import java.math.BigDecimal;

public record SeatReservedEvent(
        EventMetaData metaData,
        Long bookingId,
        String username,
        String flightNumber,
        String seatNumber,
        BigDecimal price
) {
}
