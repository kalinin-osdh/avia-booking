package ru.kalinin.booking.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.kalinin.booking.entity.Booking;
import ru.kalinin.booking.service.interfaces.BookingService;
import ru.kalinin.common.kafka.event.EventMetaData;
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

        PaymentCreatedEvent createdEvent = new PaymentCreatedEvent(
                new EventMetaData(
                        UUID.randomUUID(),
                        LocalDateTime.now()
                ),
                event.bookingNumber(),
                event.username(),
                event.price()
        );

        bookingProducer.sendPaymentCreated(createdEvent);
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

        BookingPaymentSuccessfulEvent savedEvent = new BookingPaymentSuccessfulEvent(
                new EventMetaData(
                        UUID.randomUUID(),
                        LocalDateTime.now()
                ),
                booking.getBookingNumber(),
                booking.getFlightNumber(),
                booking.getSeatNumber()
        );

        bookingProducer.sendPaymentSuccess(savedEvent);
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED
    )
    public void paymentSuccessListener(PaymentFailedEvent event) {
        bookingService.failPayment(event.bookingNumber());

        Booking booking = bookingService.getByBookingNumber(event.bookingNumber());

        BookingPaymentFailedEvent savedEvent = new BookingPaymentFailedEvent(
                new EventMetaData(
                        UUID.randomUUID(),
                        LocalDateTime.now()
                ),
                booking.getBookingNumber(),
                booking.getFlightNumber(),
                booking.getSeatNumber(),
                event.reason()
        );

        bookingProducer.sendPaymentFailed(savedEvent);
    }
}

