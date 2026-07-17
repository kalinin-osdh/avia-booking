package ru.kalinin.flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.Seat;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findSeatsByFlight(Flight flight);
}
