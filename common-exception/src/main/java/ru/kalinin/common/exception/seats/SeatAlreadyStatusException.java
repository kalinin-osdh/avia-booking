package ru.kalinin.common.exception.seats;

import ru.kalinin.common.exception.model.CustomException;

public class SeatAlreadyStatusException extends CustomException {
    public SeatAlreadyStatusException(String seatNumber, String status) {
        super("Место " + seatNumber + " не " + status);
    }
}
