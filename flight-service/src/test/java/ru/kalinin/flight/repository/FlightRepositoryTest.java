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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть false если места с таким flightNumber не существует")
    void shouldReturnFalseWhenFlightDoesNotExist() {
        boolean result = flightRepository.existsFlightByFlightNumber("someNumber");

        assertThat(result).isFalse();
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

        assertThat(actualPage.getContent())
                .hasSize(1)
                .extracting(Flight::getId)
                .containsExactly(flight1.getId());
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

        assertThat(actualPage.getContent())
                .hasSize(2)
                .extracting(Flight::getId)
                .containsExactly(
                        flight1.getId(),
                        flight2.getId()
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

        assertThat(actualPage.getContent())
                .hasSize(2)
                .extracting(Flight::getId)
                .containsExactly(
                        flight1.getId(),
                        flight3.getId()
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

        assertThat(actualPage.getContent())
                .hasSize(3)
                .extracting(Flight::getId)
                .containsExactly(
                        flight1.getId(),
                        flight2.getId(),
                        flight3.getId()
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

        assertThat(actualFlight.getId()).isEqualTo(flight.getId());
        assertThat(Hibernate.isInitialized(actualFlight.getSeats())).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional если полет не найден по flightNumber")
    void shouldReturnEmptyOptionalWhenFlightNumberDoesNotExist() {
        Optional<Flight> actualFlight = flightRepository.findByFlightNumber("someNumber");

        assertThat(actualFlight).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть информацию о кол-ве мест (свободно и всего)")
    void shouldReturnCountSeats() {
        Flight flight = TestDataFactory.createFlight();

        Seat availableSeat = TestDataFactory.createSeat(flight, "1S", SeatStatus.AVAILABLE);
        Seat reservedSeat = TestDataFactory.createSeat(flight, "2S", SeatStatus.RESERVED);
        Seat soldSeat = TestDataFactory.createSeat(flight, "3S", SeatStatus.SOLD);

        testEntityManager.persist(flight);
        testEntityManager.persist(availableSeat);
        testEntityManager.persist(reservedSeat);
        testEntityManager.persist(soldSeat);
        testEntityManager.flush();
        testEntityManager.clear();

        SeatCountsResponse countsResponse = flightRepository.findCountSeats(flight.getId());

        assertThat(countsResponse)
                .extracting(
                        SeatCountsResponse::getAvailableSeats,
                        SeatCountsResponse::getTotalSeats
                )
                .containsExactly(
                        1L,
                        3L
                );

        assertEquals(1, countsResponse.getAvailableSeats());
        assertEquals(3, countsResponse.getTotalSeats());
    }
}
