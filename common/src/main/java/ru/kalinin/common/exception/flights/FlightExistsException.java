package ru.kalinin.common.exception.flights;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.kalinin.common.exception.CustomException;

public class FlightExistsException extends CustomException {

    public FlightExistsException(String flightNumber) {
        super("Полет с таким номером уже существует: " + flightNumber);
    }
}
