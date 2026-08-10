package ru.kalinin.booking.service.interfaces;

import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.booking.dto.request.BookingRequest;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.entity.Booking;
import ru.kalinin.booking.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponse booking(String username, BookingRequest request);

    List<BookingResponse> getUserHistory(String username);

    boolean confirmBooking(Long id, BigDecimal price);

    boolean declineBooking(Long id);

    boolean successPayment(UUID bookingNumber);

    boolean failPayment(UUID bookingNumber);

    Booking getById(Long id);

    Booking getByBookingNumber(UUID bookingNumber);
}
