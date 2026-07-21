package ru.kalinin.flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.Seat;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findSeatsByFlight(Flight flight);

    @Query("""
            SELECT s
            FROM Seat s
            JOIN FETCH s.flight
            WHERE s.id = :id
            """)
    Optional<Seat> findByIdWithFlight(@Param("id") Long id);

    Seat findSeatBySeatNumber(String seatNumber);
}
