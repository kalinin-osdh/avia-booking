package ru.kalinin.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.entity.Booking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> getBookingsByUsername(String username);

    Optional<Booking> findByBookingNumber(UUID bookingNumber);
}
