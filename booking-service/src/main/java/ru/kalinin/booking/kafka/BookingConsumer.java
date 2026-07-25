package ru.kalinin.booking.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.kalinin.booking.entity.Booking;
import ru.kalinin.booking.service.interfaces.BookingService;
import ru.kalinin.common.kafka.event.EventMetadata;
import ru.kalinin.common.kafka.event.booking.BookingPaymentFailedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.payment.PaymentCreatedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentFailedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservationFailedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingConsumer {
    private final BookingService bookingService;
    private final BookingProducer bookingProducer;

    @KafkaListener(
            topics = KafkaTopics.SEAT_RESERVED
    )
    public void seatReservedListener(SeatReservedEvent event) {
        bookingService.confirmBooking(event.bookingId(), event.price());

        bookingProducer.sendPaymentCreated(
                PaymentCreatedEvent.of(
                        event.bookingNumber(),
                        event.username(),
                        event.price()
                )
        );
    }

    @KafkaListener(
            topics = KafkaTopics.SEAT_RESERVATION_FAILED
    )
    public void seatReservationFailedListener(SeatReservationFailedEvent event) {
        bookingService.declineBooking(event.bookingId());
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCESSFUL
    )
    public void paymentSuccessListener(PaymentSuccessfulEvent event) {
        bookingService.successPayment(event.bookingNumber());

        Booking booking = bookingService.getByBookingNumber(event.bookingNumber());

        bookingProducer.sendPaymentSuccess(
                BookingPaymentSuccessfulEvent.of(
                        booking.getBookingNumber(),
                        booking.getFlightNumber(),
                        booking.getSeatNumber()
                )
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED
    )
    public void paymentSuccessListener(PaymentFailedEvent event) {
        bookingService.failPayment(event.bookingNumber());

        Booking booking = bookingService.getByBookingNumber(event.bookingNumber());

        bookingProducer.sendPaymentFailed(
                BookingPaymentFailedEvent.of(
                        booking.getBookingNumber(),
                        booking.getFlightNumber(),
                        booking.getSeatNumber(),
                        event.reason()
                )
        );
    }
}

