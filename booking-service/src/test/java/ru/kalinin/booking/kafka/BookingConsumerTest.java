package ru.kalinin.booking.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kalinin.booking.entity.Booking;
import ru.kalinin.booking.factory.TestDataFactory;
import ru.kalinin.booking.service.interfaces.BookingService;
import ru.kalinin.common.kafka.event.booking.BookingPaymentFailedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.payment.PaymentCreatedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentFailedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservationFailedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservedEvent;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("booking-consumer")
public class BookingConsumerTest {
    @Mock
    private BookingService bookingService;
    @Mock
    private BookingProducer bookingProducer;

    @InjectMocks
    private BookingConsumer bookingConsumer;

    @Test
    @DisplayName("Должен изменить статус бронирования при подтверждении бронирования и отправить соответствующее событие")
    public void shouldConfirmBookingAndSendPaymentCreatedEvent() {
        SeatReservedEvent event = SeatReservedEvent.of(
                1L,
                UUID.randomUUID(),
                "kalinin",
                "1A",
                "1S",
                BigDecimal.valueOf(1000)
        );

        when(bookingService.confirmBooking(event.bookingId(), event.price())).thenReturn(true);

        bookingConsumer.seatReservedListener(event);

        ArgumentCaptor<PaymentCreatedEvent> captor = ArgumentCaptor.forClass(PaymentCreatedEvent.class);

        verify(bookingProducer).sendPaymentCreated(captor.capture());

        PaymentCreatedEvent actual = captor.getValue();

        assertThat(actual).isNotNull()
                .extracting(
                        PaymentCreatedEvent::bookingNumber,
                        PaymentCreatedEvent::username,
                        PaymentCreatedEvent::price
                )
                .containsExactly(
                        event.bookingNumber(),
                        event.username(),
                        event.price()
                );

        verify(bookingService).confirmBooking(event.bookingId(), event.price());
    }

    @Test
    @DisplayName("Должен изменить статус бронирования при отклонении бронирования")
    public void shouldDeclineBookingWhenSeatReservationFailed() {
        SeatReservationFailedEvent event = SeatReservationFailedEvent.of(
                1L,
                UUID.randomUUID(),
                "some reason"
        );

        bookingConsumer.seatReservationFailedListener(event);

        verify(bookingService).declineBooking(event.bookingId());
        verifyNoInteractions(bookingProducer);
    }

    @Test
    @DisplayName("Должен изменить статус бронирования при подтверждении платежа и отправить соответствующее событие")
    public void shouldSuccessPaymentAndSendPaymentSuccessEvent() {
        PaymentSuccessfulEvent event = PaymentSuccessfulEvent.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "kalinin",
                BigDecimal.valueOf(1000)
        );

        Booking booking = TestDataFactory.createBooking(1L, "1A", "1S");

        when(bookingService.successPayment(event.bookingNumber())).thenReturn(true);
        when(bookingService.getByBookingNumber(event.bookingNumber())).thenReturn(booking);

        bookingConsumer.paymentSuccessListener(event);

        ArgumentCaptor<BookingPaymentSuccessfulEvent> captor = ArgumentCaptor.forClass(BookingPaymentSuccessfulEvent.class);

        verify(bookingProducer).sendPaymentSuccess(captor.capture());

        BookingPaymentSuccessfulEvent actual = captor.getValue();

        assertThat(actual).isNotNull()
                .extracting(
                        BookingPaymentSuccessfulEvent::bookingNumber,
                        BookingPaymentSuccessfulEvent::flightNumber,
                        BookingPaymentSuccessfulEvent::seatNumber
                )
                .containsExactly(
                        booking.getBookingNumber(),
                        booking.getFlightNumber(),
                        booking.getSeatNumber()
                );

        verify(bookingService).successPayment(event.bookingNumber());
        verify(bookingService).getByBookingNumber(event.bookingNumber());
    }

    @Test
    @DisplayName("Должен изменить статус бронирования при отклонении платежа и отправить соответствующее событие")
    public void shouldFailPaymentAndSendPaymentFailedEvent() {
        PaymentFailedEvent event = PaymentFailedEvent.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "kalinin",
                BigDecimal.valueOf(1000),
                "some reason"
        );

        Booking booking = TestDataFactory.createBooking(1L, "1A", "1S");

        when(bookingService.failPayment(event.bookingNumber())).thenReturn(true);
        when(bookingService.getByBookingNumber(event.bookingNumber())).thenReturn(booking);

        bookingConsumer.paymentFailListener(event);

        ArgumentCaptor<BookingPaymentFailedEvent> captor = ArgumentCaptor.forClass(BookingPaymentFailedEvent.class);

        verify(bookingProducer).sendPaymentFailed(captor.capture());

        BookingPaymentFailedEvent actual = captor.getValue();

        assertThat(actual).isNotNull()
                .extracting(
                        BookingPaymentFailedEvent::bookingNumber,
                        BookingPaymentFailedEvent::flightNumber,
                        BookingPaymentFailedEvent::seatNumber,
                        BookingPaymentFailedEvent::reason
                )
                .containsExactly(
                        booking.getBookingNumber(),
                        booking.getFlightNumber(),
                        booking.getSeatNumber(),
                        event.reason()
                );

        verify(bookingService).failPayment(event.bookingNumber());
        verify(bookingService).getByBookingNumber(event.bookingNumber());
    }
}
