package ru.kalinin.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.entity.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> getBookingsByUsername(String username);
}
