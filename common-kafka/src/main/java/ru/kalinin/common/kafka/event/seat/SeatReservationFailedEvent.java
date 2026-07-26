package ru.kalinin.common.kafka.event.seat;

import ru.kalinin.common.kafka.event.EventMetadata;

import java.util.UUID;

public record SeatReservationFailedEvent(
        EventMetadata metadata,
        Long bookingId,
        UUID bookingNumber,
        String reason
) {
    public static SeatReservationFailedEvent of(
            Long bookingId,
            UUID bookingNumber,
            String reason
    ) {
        return new SeatReservationFailedEvent(
                EventMetadata.create(),
                bookingId,
                bookingNumber,
                reason
        );
    }
}
