package ru.kalinin.notification.util;

import org.springframework.stereotype.Component;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentFailedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.payment.PaymentCreatedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentFailedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservationFailedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservedEvent;

@Component
public class MessageBuilder {

    public String build(BookingCreatedEvent event) {
        return """
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
        );
    }

    public String build(SeatReservedEvent event) {
        return """
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
        );
    }

    public String build(SeatReservationFailedEvent event) {
        return """
                Место не свободно:
                
                Бронь: %s
                Причина: %s
                """.formatted(
                event.bookingNumber(),
                event.reason()
        );
    }

    public String build(PaymentCreatedEvent event) {
        return """
                Платеж создан:
                
                Бронь: %s
                Пользователь: %s
                Цена: %s
                """.formatted(
                event.bookingNumber(),
                event.username(),
                event.price()
        );
    }

    public String build(PaymentSuccessfulEvent event) {
        return """
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
        );
    }

    public String build(PaymentFailedEvent event) {
        return """
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
        );
    }

    public String build(BookingPaymentSuccessfulEvent event) {
        return """
                Бронь оплачена:
                
                Бронь: %s
                Номер самолета: %s
                Номер места: %s
                """.formatted(
                event.bookingNumber(),
                event.flightNumber(),
                event.seatNumber()
        );
    }

    public String build(BookingPaymentFailedEvent event) {
        return """
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
        );
    }
}
