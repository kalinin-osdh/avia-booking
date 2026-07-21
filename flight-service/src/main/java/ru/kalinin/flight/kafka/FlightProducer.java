package ru.kalinin.flight.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.kalinin.common.kafka.event.seat.SeatReservedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservationFailedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;

@Service
@RequiredArgsConstructor
public class FlightProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendSeatReserved(SeatReservedEvent event) {
        kafkaTemplate.send(
                KafkaTopics.SEAT_RESERVED,
                event
        );
    }

    public void sendSeatReservationFailed(SeatReservationFailedEvent event) {
        kafkaTemplate.send(
                KafkaTopics.SEAT_RESERVATION_FAILED,
                event
        );
    }
}
