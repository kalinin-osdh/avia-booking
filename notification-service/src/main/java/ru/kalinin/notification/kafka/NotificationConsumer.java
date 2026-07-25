package ru.kalinin.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentFailedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.payment.PaymentCreatedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentFailedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservationFailedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;
import ru.kalinin.notification.service.TelegramService;
import ru.kalinin.notification.util.MessageBuilder;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {
    private final TelegramService telegramService;
    private final MessageBuilder messageBuilder;

    @KafkaListener(
            topics = KafkaTopics.BOOKING_CREATED
    )
    public void sendNotification(BookingCreatedEvent event) {
        telegramService.sendMessage(
                messageBuilder.build(event)
        );
    }

    @KafkaListener(
            topics = KafkaTopics.SEAT_RESERVED
    )
    public void sendNotification(SeatReservedEvent event) {
        telegramService.sendMessage(
                messageBuilder.build(event)
        );
    }

    @KafkaListener(
            topics = KafkaTopics.SEAT_RESERVATION_FAILED
    )
    public void sendNotification(SeatReservationFailedEvent event) {
        telegramService.sendMessage(
                messageBuilder.build(event)
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_CREATED
    )
    public void sendNotification(PaymentCreatedEvent event) {
        telegramService.sendMessage(
                messageBuilder.build(event)
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCESSFUL
    )
    public void sendNotification(PaymentSuccessfulEvent event) {
        telegramService.sendMessage(
                messageBuilder.build(event)
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED
    )
    public void sendNotification(PaymentFailedEvent event) {
        telegramService.sendMessage(
                messageBuilder.build(event)
        );
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_PAYMENT_SUCCESSFUL
    )
    public void sendNotification(BookingPaymentSuccessfulEvent event) {
        telegramService.sendMessage(
                messageBuilder.build(event)
        );
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_PAYMENT_FAILED
    )
    public void sendNotification(BookingPaymentFailedEvent event) {
        telegramService.sendMessage(
                messageBuilder.build(event)
        );
    }
}
