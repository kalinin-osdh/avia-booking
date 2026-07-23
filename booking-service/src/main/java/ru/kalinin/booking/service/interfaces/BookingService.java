package ru.kalinin.booking.service.interfaces;

import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.booking.dto.request.BookingRequest;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.entity.Booking;
import ru.kalinin.booking.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.util.List;

public interface BookingService {
    BookingResponse booking(String username, BookingRequest request);

    List<BookingResponse> getUserHistory(String username);

    void confirmBooking(Long id, BigDecimal price);

    void declineBooking(Long id);

    Booking getById(Long id);
}
