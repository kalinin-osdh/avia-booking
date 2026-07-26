package ru.kalinin.common.exception.payments;

import ru.kalinin.common.exception.model.CustomException;

import java.util.UUID;

public class PaymentUserNotEqualsException extends CustomException {

    public PaymentUserNotEqualsException(String username, UUID bookingNumber) {
        super("Бронь с номером: " + bookingNumber + " не принадлежит пользователю " + username);
    }
}
