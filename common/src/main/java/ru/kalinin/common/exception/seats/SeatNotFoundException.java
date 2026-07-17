package ru.kalinin.common.exception.seats;

import ru.kalinin.common.exception.NotFoundException;

public class SeatNotFoundException extends NotFoundException {

    public SeatNotFoundException(Long id) {
        super("Место не найдено id: " + id);
    }
}
