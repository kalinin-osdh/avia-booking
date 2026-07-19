package ru.kalinin.flight.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.enums.SeatStatus;

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
              AND (:status IS NULL OR s.status = :status)
            """)
    Optional<Flight> findByFlightNumberWithSeats(
            @Param("flightNumber") String flightNumber,
            @Param("status") SeatStatus status
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Flight f
            SET f.availableSeats = f.availableSeats + :delta
            WHERE f.id = :id
                AND f.availableSeats + :delta >= 0
                AND f.availableSeats + :delta <= f.totalSeats
            """)
    int updateAvailableSeats(
            @Param("id") Long id,
            @Param("delta") Integer delta
            );
}
