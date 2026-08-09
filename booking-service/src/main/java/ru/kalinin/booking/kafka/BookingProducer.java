package ru.kalinin.booking.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentFailedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.payment.PaymentCreatedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;

@Service
@RequiredArgsConstructor
public class BookingProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBookingCreated(BookingCreatedEvent event) {
        kafkaTemplate.send(
                KafkaTopics.BOOKING_CREATED,
                event
        );
    }

    public void sendPaymentCreated(PaymentCreatedEvent event) {
        kafkaTemplate.send(
                KafkaTopics.PAYMENT_CREATED,
                event
        );
    }

    public void sendPaymentSuccess(BookingPaymentSuccessfulEvent event) {
        kafkaTemplate.send(
                KafkaTopics.BOOKING_PAYMENT_SUCCESSFUL,
                event
        );
    }

    public void sendPaymentFailed(BookingPaymentFailedEvent event) {
        kafkaTemplate.send(
                KafkaTopics.BOOKING_PAYMENT_FAILED,
                event
        );
    }
}
