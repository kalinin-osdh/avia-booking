package ru.kalinin.flight.repository;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.Seat;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("seat-repository")
public class SeatRepositoryTest extends RepositoryTest {
    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("Должен найти все места для полета")
    void shouldReturnSeatsByFlight() {
        System.out.println(seatRepository.count());
        Flight flight = TestDataFactory.createFlight();
        Seat seat = TestDataFactory.createSeat(flight);

        testEntityManager.persist(flight);
        testEntityManager.persist(seat);
        testEntityManager.flush();
        testEntityManager.clear();

        List<Seat> actualSeats = seatRepository.findSeatsByFlight(flight);

        assertEquals(1, actualSeats.size());
        assertEquals(seat.getId(), actualSeats.get(0).getId());
    }

    @Test
    @DisplayName("Должен вернуть пустой список для полета у которого нет мест")
    void shouldReturnEmptySeatsListByFlight() {
        Flight flight = TestDataFactory.createFlight();

        testEntityManager.persist(flight);
        testEntityManager.flush();
        testEntityManager.clear();

        List<Seat> actualSeats = seatRepository.findSeatsByFlight(flight);

        assertTrue(actualSeats.isEmpty());
    }

    @Test
    @DisplayName("Должен вернуть место по ID и подгрузить полет которому принадлежит")
    void shouldReturnSeatWithFlightWhenSeatExists() {
        Flight flight = TestDataFactory.createFlight();
        Seat seat = TestDataFactory.createSeat(flight);

        testEntityManager.persist(flight);
        testEntityManager.persist(seat);
        testEntityManager.flush();
        testEntityManager.clear();

        Seat actualSeat = seatRepository.findByIdWithFlight(seat.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(seat.getId(), actualSeat.getId()),
                () -> assertEquals(flight.getFlightNumber(), actualSeat.getFlight().getFlightNumber()),
                () -> assertTrue(Hibernate.isInitialized(actualSeat.getFlight()))
        );
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional когда такого места не существует")
    void shouldReturnEmptyOptionalWhenSeatDoesNotExist() {
        Optional<Seat> actualSeat = seatRepository.findByIdWithFlight(Long.MAX_VALUE);

        assertTrue(actualSeat.isEmpty());
    }
}
