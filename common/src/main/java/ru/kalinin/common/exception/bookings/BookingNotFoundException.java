package ru.kalinin.common.exception.bookings;

import ru.kalinin.common.exception.NotFoundException;

import java.util.UUID;

public class BookingNotFoundException extends NotFoundException {
    public BookingNotFoundException(Long id) {
        super("Запись о бронировании не найдена: " + id);
    }

    public BookingNotFoundException(UUID bookingNumber) {
        super("Запись о бронировании не найдена: " + bookingNumber);
    }
}
