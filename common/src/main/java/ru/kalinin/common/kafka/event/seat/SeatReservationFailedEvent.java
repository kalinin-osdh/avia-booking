package ru.kalinin.common.kafka.event.seat;

import ru.kalinin.common.kafka.event.EventMetaData;

public record SeatReservationFailedEvent(
        EventMetaData metaData,
        Long bookingId,
        String reason
) {

}
