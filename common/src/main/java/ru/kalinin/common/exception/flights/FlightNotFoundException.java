package ru.kalinin.common.exception.flights;

import ru.kalinin.common.exception.NotFoundException;

public class FlightNotFoundException extends NotFoundException {

    public FlightNotFoundException(Long id) {
        super("Полет не найден: id = " + id);
    }

    public FlightNotFoundException(String flightNumber) {
        super("Полет не найден: номер полета = " + flightNumber);
    }
}
