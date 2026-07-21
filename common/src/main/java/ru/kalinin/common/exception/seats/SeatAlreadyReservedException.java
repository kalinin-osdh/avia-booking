package ru.kalinin.common.exception.seats;

import ru.kalinin.common.exception.CustomException;

public class SeatAlreadyReservedException extends CustomException {
    public SeatAlreadyReservedException(String seatNumber) {
        super("Место " + seatNumber + " уже забронировано");
    }
}
