package ru.kalinin.common.kafka.event.seat;

import ru.kalinin.common.kafka.event.EventMetaData;

import java.util.UUID;

public record SeatReservationFailedEvent(
        EventMetaData metaData,
        Long bookingId,
        UUID bookingNumber,
        String reason
) {

}
