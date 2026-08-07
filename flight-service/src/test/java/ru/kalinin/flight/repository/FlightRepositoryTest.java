package ru.kalinin.flight.repository;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.kalinin.flight.dto.response.SeatCountsResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.Seat;
import ru.kalinin.flight.entity.enums.SeatStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("flight-repository")
public class FlightRepositoryTest extends RepositoryTest {
    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("Должен вернуть true если место с таким flightNumber существует")
    void shouldReturnTrueWhenFlightExists() {
        Flight flight = TestDataFactory.createFlight();

        testEntityManager.persistAndFlush(flight);
        testEntityManager.clear();

        boolean result = flightRepository.existsFlightByFlightNumber(flight.getFlightNumber());

        assertTrue(result);
    }

    @Test
    @DisplayName("Должен вернуть false если места с таким flightNumber не существует")
    void shouldReturnFalseWhenFlightDoesNotExist() {
        boolean result = flightRepository.existsFlightByFlightNumber("someNumber");

        assertFalse(result);
    }

    @Test
    @DisplayName("Должен вернуть страницу с отфильтрованными данными по городу вылета и прибытия")
    void shouldReturnFlightPageWhenFilteringByDepartureAndArrivalCity() {
        Flight flight1 = TestDataFactory.createFlight();
        Flight flight2 = TestDataFactory.createFlight("1B", "MOSCOW", "ST. PETERSBURG");
        Flight flight3 = TestDataFactory.createFlight("1C", "SOCHI", "MOSCOW");

        testEntityManager.persist(flight1);
        testEntityManager.persist(flight2);
        testEntityManager.persist(flight3);
        testEntityManager.flush();
        testEntityManager.clear();

        Pageable pageable = PageRequest.of(0, 5);

        Page<Flight> actualPage = flightRepository.findAllWithFilter(
                "MOSCOW",
                "SOCHI",
                pageable
        );

        assertEquals(1, actualPage.getContent().size());
        assertEquals(flight1.getId(), actualPage.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Должен вернуть страницу с отфильтрованными данными по городу вылета")
    void shouldReturnFlightPageWhenFilteringByDepartureCity() {
        Flight flight1 = TestDataFactory.createFlight();
        Flight flight2 = TestDataFactory.createFlight("1B", "MOSCOW", "ST. PETERSBURG");
        Flight flight3 = TestDataFactory.createFlight("1C", "SOCHI", "MOSCOW");

        testEntityManager.persist(flight1);
        testEntityManager.persist(flight2);
        testEntityManager.persist(flight3);
        testEntityManager.flush();
        testEntityManager.clear();

        Pageable pageable = PageRequest.of(0, 5);

        Page<Flight> actualPage = flightRepository.findAllWithFilter(
                "MOSCOW",
                null,
                pageable
        );

        List<Long> listOfId = actualPage.getContent().stream().map(Flight::getId).toList();

        assertEquals(2, actualPage.getContent().size());
        assertAll(
                () -> assertTrue(listOfId.contains(flight1.getId())),
                () -> assertTrue(listOfId.contains(flight2.getId()))
        );
    }

    @Test
    @DisplayName("Должен вернуть страницу с отфильтрованными данными по городу прибытия")
    void shouldReturnFlightPageWhenFilteringByArrivalCity() {
        Flight flight1 = TestDataFactory.createFlight();
        Flight flight2 = TestDataFactory.createFlight("1B", "MOSCOW", "ST. PETERSBURG");
        Flight flight3 = TestDataFactory.createFlight("1C", "ST. PETERSBURG", "SOCHI");

        testEntityManager.persist(flight1);
        testEntityManager.persist(flight2);
        testEntityManager.persist(flight3);
        testEntityManager.flush();
        testEntityManager.clear();

        Pageable pageable = PageRequest.of(0, 5);

        Page<Flight> actualPage = flightRepository.findAllWithFilter(
                null,
                "SOCHI",
                pageable
        );

        List<Long> listOfId = actualPage.getContent().stream().map(Flight::getId).toList();

        assertEquals(2, actualPage.getContent().size());
        assertAll(
                () -> assertTrue(listOfId.contains(flight1.getId())),
                () -> assertTrue(listOfId.contains(flight3.getId()))
        );
    }

    @Test
    @DisplayName("Должен вернуть страницу без фильтрации данных")
    void shouldReturnFlightPageWhenFiltersAreEmpty() {
        Flight flight1 = TestDataFactory.createFlight();
        Flight flight2 = TestDataFactory.createFlight("1B", "MOSCOW", "ST. PETERSBURG");
        Flight flight3 = TestDataFactory.createFlight("1C", "SOCHI", "MOSCOW");

        testEntityManager.persist(flight1);
        testEntityManager.persist(flight2);
        testEntityManager.persist(flight3);
        testEntityManager.flush();
        testEntityManager.clear();

        Pageable pageable = PageRequest.of(0, 5);

        Page<Flight> actualPage = flightRepository.findAllWithFilter(
                null,
                null,
                pageable
        );

        List<Long> listOfId = actualPage.getContent().stream().map(Flight::getId).toList();

        assertEquals(3, actualPage.getContent().size());
        assertAll(
                () -> assertTrue(listOfId.contains(flight1.getId())),
                () -> assertTrue(listOfId.contains(flight2.getId())),
                () -> assertTrue(listOfId.contains(flight3.getId()))
        );
    }

    @Test
    @DisplayName("Должен вернуть полет по flightNumber")
    void shouldReturnFlightWhenFlightNumberExists() {
        Flight flight = TestDataFactory.createFlight();
        Seat seat = TestDataFactory.createSeat(flight);

        testEntityManager.persist(flight);
        testEntityManager.persist(seat);
        testEntityManager.flush();
        testEntityManager.clear();

        Flight actualFlight = flightRepository.findByFlightNumber(flight.getFlightNumber()).orElseThrow();

        assertEquals(flight.getId(), actualFlight.getId());
        assertTrue(Hibernate.isInitialized(actualFlight.getSeats()));
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional если полет не найден по flightNumber")
    void shouldReturnEmptyOptionalWhenFlightNumberDoesNotExist() {
        Optional<Flight> actualFlight = flightRepository.findByFlightNumber("someNumber");

        assertTrue(actualFlight.isEmpty());
    }

    @Test
    @DisplayName("Должен вернуть информацию о кол-ве мест (свободно и всего)")
    void shouldReturnCountSeats() {
        Flight flight = TestDataFactory.createFlight();

        Seat availableSeat = TestDataFactory.createSeat(flight, "1S",SeatStatus.AVAILABLE);
        Seat reservedSeat = TestDataFactory.createSeat(flight, "2S",SeatStatus.RESERVED);
        Seat soldSeat = TestDataFactory.createSeat(flight, "3S",SeatStatus.SOLD);

        testEntityManager.persist(flight);
        testEntityManager.persist(availableSeat);
        testEntityManager.persist(reservedSeat);
        testEntityManager.persist(soldSeat);
        testEntityManager.flush();
        testEntityManager.clear();

        SeatCountsResponse countsResponse = flightRepository.findCountSeats(flight.getId());

        assertEquals(1, countsResponse.getAvailableSeats());
        assertEquals(3, countsResponse.getTotalSeats());
    }
}
