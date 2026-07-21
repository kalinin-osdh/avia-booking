package ru.kalinin.booking.service.interfaces;

import ru.kalinin.booking.dto.request.BookingRequest;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.entity.Booking;

import java.util.List;

public interface BookingService {
    BookingResponse booking(String username, BookingRequest request);
    List<BookingResponse> getUserHistory(String username);

}
