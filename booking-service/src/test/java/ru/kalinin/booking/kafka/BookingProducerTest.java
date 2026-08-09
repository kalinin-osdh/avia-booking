package ru.kalinin.booking.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentFailedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.payment.PaymentCreatedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("booking-producer")
public class BookingProducerTest {
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private BookingProducer bookingProducer;

    @Test
    @DisplayName("Должен отправить событие в Кафка-топик BOOKING_CREATED")
    void shouldSendBookingCreated() {
        BookingCreatedEvent event = BookingCreatedEvent.of(
                1L,
                UUID.randomUUID(),
                "kalinin",
                "1A",
                "1S"
        );

        bookingProducer.sendBookingCreated(event);

        verify(kafkaTemplate).send(KafkaTopics.BOOKING_CREATED, event);
    }

    @Test
    @DisplayName("Должен отправить событие в Кафка-топик PAYMENT_CREATED")
    void shouldSendPaymentCreated() {
        PaymentCreatedEvent event = PaymentCreatedEvent.of(
                UUID.randomUUID(),
                "kalinin",
                BigDecimal.valueOf(1000)
        );

        bookingProducer.sendPaymentCreated(event);

        verify(kafkaTemplate).send(KafkaTopics.PAYMENT_CREATED, event);
    }

    @Test
    @DisplayName("Должен отправить событие в Кафка-топик BOOKING_PAYMENT_SUCCESSFUL")
    void shouldSendPaymentSuccess() {
        BookingPaymentSuccessfulEvent event = BookingPaymentSuccessfulEvent.of(
                UUID.randomUUID(),
                "1A",
                "1S"
        );

        bookingProducer.sendPaymentSuccess(event);

        verify(kafkaTemplate).send(KafkaTopics.BOOKING_PAYMENT_SUCCESSFUL, event);
    }

    @Test
    @DisplayName("Должен отправить событие в Кафка-топик BOOKING_PAYMENT_FAILED")
    void shouldSendPaymentFailed() {
        BookingPaymentFailedEvent event =  BookingPaymentFailedEvent.of(
                UUID.randomUUID(),
                "1A",
                "1S",
                "some reason"
        );

        bookingProducer.sendPaymentFailed(event);

        verify(kafkaTemplate).send(KafkaTopics.BOOKING_PAYMENT_FAILED, event);
    }
}
