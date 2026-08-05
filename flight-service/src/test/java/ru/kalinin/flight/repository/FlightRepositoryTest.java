package ru.kalinin.flight.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@Tag("flight-repository")
public class FlightRepositoryTest extends RepositoryTest {
    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("")
    void shouldExistsFlightByFlightNumber() {
        // todo сущность найдена -> true
    }

    @Test
    @DisplayName("")
    void shouldReturnFalseWhenExistsFlightByFlightNumber() {
        // todo сущность не найдена -> false
    }

    @Test
    @DisplayName("")
    void shouldFindAllWithAllFilters() {
        // todo поиск flight со всеми фильтрами
    }

    @Test
    @DisplayName("")
    void shouldFindAllWithFilterDepartureCity() {
        // todo поиск flight с фильтром по отправлению
    }

    @Test
    @DisplayName("")
    void shouldFindAllWithFilterArrivalCity() {
        // todo поиск flight с фильтром по прибытию
    }

    @Test
    @DisplayName("")
    void shouldFindAllWithoutFilter() {
        // todo поиск flight БЕЗ фильтров
    }

    @Test
    @DisplayName("")
    void shouldFindByFlightNumber() {
        // todo поиск по f.num
    }

    @Test
    @DisplayName("")
    void shouldReturnEmptyOptionalWhenFindByFlightNumber() {
        // todo return empty optional при поиске по f.num когда flight не существует
    }

    @Test
    @DisplayName("")
    void shouldFindCountSeats() {
        // todo успешный подсчет
    }
}
