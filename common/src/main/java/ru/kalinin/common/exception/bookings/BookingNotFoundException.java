package ru.kalinin.common.exception.bookings;

import ru.kalinin.common.exception.NotFoundException;

public class BookingNotFoundException extends NotFoundException {
    public BookingNotFoundException(Long id) {
        super("Запись о бронировании не найдена id: " + id);
    }
}
