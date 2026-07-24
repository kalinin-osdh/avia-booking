package ru.kalinin.common.exception.payments;

import ru.kalinin.common.exception.CustomException;

import java.util.UUID;

public class PaymentAlreadyExistsException extends CustomException {

    public PaymentAlreadyExistsException(UUID bookingNumber) {
        super("Оплата этой брони уже существует: " + bookingNumber);
    }
}
