package ru.kalinin.flight.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.kalinin.common.exception.flights.FlightNotFoundException;
import ru.kalinin.common.exception.seats.SeatAlreadyReservedException;
import ru.kalinin.common.exception.seats.SeatNotFoundException;
import ru.kalinin.common.kafka.event.EventMetaData;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservedFailedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;
import ru.kalinin.flight.service.interfaces.FlightService;

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
            flightService.reserveSeat(event.flightNumber(), event.seatNumber());

            SeatReservedEvent sendEvent = new SeatReservedEvent(
                    new EventMetaData(
                            UUID.randomUUID(),
                            LocalDateTime.now()
                    ),
                    event.bookingId(),
                    event.username(),
                    event.flightNumber(),
                    event.seatNumber()
            );

            flightProducer.sendSeatReserved(sendEvent);
        } catch (SeatAlreadyReservedException | FlightNotFoundException | SeatNotFoundException ex) {
            SeatReservedFailedEvent sendEvent = new SeatReservedFailedEvent(
                    new EventMetaData(
                            UUID.randomUUID(),
                            LocalDateTime.now()
                    ),
                    event.bookingId(),
                    ex.getMessage()
            );
            flightProducer.sendSeatReservationFailed(sendEvent);
        }
    }
}
