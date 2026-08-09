package ru.kalinin.flight.factory;

import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.Seat;
import ru.kalinin.flight.entity.enums.SeatStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TestDataFactory {
    private TestDataFactory() {

    }

    public static Flight createFlight() {
        return createFlight("1A", "MOSCOW", "SOCHI");
    }

    public static Flight createFlight(String flightNumber) {
        return createFlight(flightNumber, "MOSCOW", "SOCHI");
    }

    public static Flight createFlight(String flightNumber, String departureCity, String arrivalCity) {
        return Flight.builder()
                .flightNumber(flightNumber)
                .departureCity(departureCity)
                .arrivalCity(arrivalCity)
                .departureTime(LocalDateTime.now().plusDays(2))
                .arrivalTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .build();
    }

    public static Seat createSeat(Flight flight) {
        return createSeat(flight, "1S", SeatStatus.AVAILABLE);
    }

    public static Seat createSeat(Flight flight, String number, SeatStatus status) {
        return Seat.builder()
                .flight(flight)
                .seatNumber(number)
                .status(status)
                .price(BigDecimal.valueOf(1000))
                .build();
    }
}
