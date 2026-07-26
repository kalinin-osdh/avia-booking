package ru.kalinin.common.exception.flights;

import ru.kalinin.common.exception.model.CustomException;

public class FlightExistsException extends CustomException {

    public FlightExistsException(String flightNumber) {
        super("Полет с таким номером уже существует: " + flightNumber);
    }
}
