package ru.kalinin.common.kafka.dto.booking;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingCreatedEvent(
        EventMetaData metaData,
        Long bookingId,
        String username,
        String flightNumber,
        String seatNumber
) {

}
