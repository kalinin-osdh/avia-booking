package ru.kalinin.common.exception.booking;

import ru.kalinin.common.exception.NotFoundException;

public class BookingNotFoundException extends NotFoundException {
    public BookingNotFoundException(Long id) {
        super("Запись о бронировании не найдена id: " + id);
    }
}
