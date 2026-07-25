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

@Service
@RequiredArgsConstructor
public class NotificationConsumer {
    private final TelegramService telegramService;

    @KafkaListener(
            topics = KafkaTopics.BOOKING_CREATED
    )
    public void sendNotification(BookingCreatedEvent event) {
        telegramService.sendMessage(
                """
                        Создана запись о бронировании
                        
                        Бронь: %s
                        Пользователь: %s
                        Номер самолета: %s
                        Номер места: %s
                        """.formatted(
                        event.bookingNumber(),
                        event.username(),
                        event.flightNumber(),
                        event.seatNumber()
                )
        );
    }

    @KafkaListener(
            topics = KafkaTopics.SEAT_RESERVED
    )
    public void sendNotification(SeatReservedEvent event) {
        telegramService.sendMessage(
                """
                        Место свободно:
                        
                        Бронь: %s
                        Пользователь: %s
                        Номер самолета: %s
                        Номер места: %s
                        Цена места: %s
                        """.formatted(
                        event.bookingNumber(),
                        event.username(),
                        event.flightNumber(),
                        event.seatNumber(),
                        event.price()
                )
        );
    }

    @KafkaListener(
            topics = KafkaTopics.SEAT_RESERVATION_FAILED
    )
    public void sendNotification(SeatReservationFailedEvent event) {
        telegramService.sendMessage(
                """
                        Место не свободно:
                        
                        Бронь: %s
                        Причина: %s
                        """.formatted(
                        event.bookingNumber(),
                        event.reason()
                )
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_CREATED
    )
    public void sendNotification(PaymentCreatedEvent event) {
        telegramService.sendMessage(
                """
                        Платеж создан:
                        
                        Бронь: %s
                        Пользователь: %s
                        Цена: %s
                        """.formatted(
                        event.bookingNumber(),
                        event.username(),
                        event.price()
                )
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCESSFUL
    )
    public void sendNotification(PaymentSuccessfulEvent event) {
        telegramService.sendMessage(
                """
                        Платеж подтвержден:
                        
                        Бронь: %s
                        Платеж: %s
                        Пользователь: %s
                        Цена: %s
                        """.formatted(
                        event.bookingNumber(),
                        event.paymentNumber(),
                        event.username(),
                        event.price()
                )
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED
    )
    public void sendNotification(PaymentFailedEvent event) {
        telegramService.sendMessage(
                """
                        Платеж отменен:
                        
                        Бронь: %s
                        Платеж: %s
                        Пользователь: %s
                        Цена: %s
                        Причина: %s
                        """.formatted(
                        event.bookingNumber(),
                        event.paymentNumber(),
                        event.username(),
                        event.price(),
                        event.reason()
                )
        );
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_PAYMENT_SUCCESSFUL
    )
    public void sendNotification(BookingPaymentSuccessfulEvent event) {
        telegramService.sendMessage(
                """
                        Бронь оплачена:
                        
                        Бронь: %s
                        Номер самолета: %s
                        Номер места: %s
                        """.formatted(
                        event.bookingNumber(),
                        event.flightNumber(),
                        event.seatNumber()
                )
        );
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_PAYMENT_FAILED
    )
    public void sendNotification(BookingPaymentFailedEvent event) {
        telegramService.sendMessage(
                """
                        Бронь не оплачена:
                        
                        Бронь: %s
                        Номер самолета: %s
                        Номер места: %s
                        Причина: %s
                        """.formatted(
                        event.bookingNumber(),
                        event.flightNumber(),
                        event.seatNumber(),
                        event.reason()
                )
        );
    }
}
