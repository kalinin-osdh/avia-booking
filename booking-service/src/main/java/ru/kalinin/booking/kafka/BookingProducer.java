package ru.kalinin.booking.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;

@Service
@RequiredArgsConstructor
public class BookingProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBookingCreated(BookingCreatedEvent event){
        kafkaTemplate.send(
                KafkaTopics.BOOKING_CREATED,
                event
        );
    }
}
