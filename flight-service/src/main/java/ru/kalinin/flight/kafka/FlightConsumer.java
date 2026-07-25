package ru.kalinin.flight.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.kalinin.common.exception.flights.FlightNotFoundException;
import ru.kalinin.common.exception.seats.SeatAlreadyStatusException;
import ru.kalinin.common.exception.seats.SeatNotFoundException;
import ru.kalinin.common.kafka.event.EventMetaData;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentFailedEvent;
import ru.kalinin.common.kafka.event.booking.BookingPaymentSuccessfulEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservationFailedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;
import ru.kalinin.flight.service.interfaces.FlightService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlightConsumer {
    private final FlightService flightService;
    private final FlightProducer flightProducer;

    @KafkaListener(
            topics = KafkaTopics.BOOKING_CREATED
    )
    public void reservationListener(BookingCreatedEvent event) {
        try {
            BigDecimal price = flightService.reserveSeat(event.flightNumber(), event.seatNumber());

            SeatReservedEvent sendEvent = new SeatReservedEvent(
                    new EventMetaData(
                            UUID.randomUUID(),
                            LocalDateTime.now()
                    ),
                    event.bookingId(),
                    event.bookingNumber(),
                    event.username(),
                    event.flightNumber(),
                    event.seatNumber(),
                    price
            );

            flightProducer.sendSeatReserved(sendEvent);
        } catch (SeatAlreadyStatusException | FlightNotFoundException | SeatNotFoundException ex) {
            SeatReservationFailedEvent sendEvent = new SeatReservationFailedEvent(
                    new EventMetaData(
                            UUID.randomUUID(),
                            LocalDateTime.now()
                    ),
                    event.bookingId(),
                    event.bookingNumber(),
                    ex.getMessage()
            );
            flightProducer.sendSeatReservationFailed(sendEvent);
        }
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_PAYMENT_SUCCESSFUL
    )
    public void bookingSuccessfulListener(BookingPaymentSuccessfulEvent event){
        flightService.soldSeat(event.flightNumber(), event.seatNumber());
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_PAYMENT_FAILED
    )
    public void bookingFailListener(BookingPaymentFailedEvent event){
        flightService.availableSeat(event.flightNumber(), event.seatNumber());
    }
}
