package ru.kalinin.flight.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kalinin.flight.dto.response.SeatCountsResponse;
import ru.kalinin.flight.entity.Flight;

import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    boolean existsFlightByFlightNumber(String flightNumber);

    @Query("""
            SELECT f
            FROM Flight f
            WHERE (:departureCity IS NULL OR f.departureCity = :departureCity)
              AND (:arrivalCity IS NULL OR f.arrivalCity = :arrivalCity)
            """)
    Page<Flight> findAllWithFilter(
            @Param("departureCity") String departureCity,
            @Param("arrivalCity") String arrivalCity,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT f
            FROM Flight f
            LEFT JOIN FETCH f.seats s
            WHERE f.flightNumber = :flightNumber
            """)
    Optional<Flight> findByFlightNumber(
            @Param("flightNumber") String flightNumber
    );

    @Query("""
            SELECT
                COUNT(s.id) as totalSeats,
                SUM(CASE WHEN s.status = 'AVAILABLE' THEN 1 ELSE 0 END) as availableSeats
            FROM Flight f
            LEFT JOIN f.seats s
            WHERE f.id = :id
            GROUP BY f.id
            """)
    SeatCountsResponse findCountSeats(@Param("id") Long id);

}
