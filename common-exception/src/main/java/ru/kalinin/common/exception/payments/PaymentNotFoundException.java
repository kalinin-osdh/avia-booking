package ru.kalinin.common.exception.payments;

import ru.kalinin.common.exception.model.NotFoundException;

import java.util.UUID;

public class PaymentNotFoundException extends NotFoundException {

    public PaymentNotFoundException(UUID bookingNumber) {
        super("Платеж не найден: " + bookingNumber);
    }

    public PaymentNotFoundException(Long id) {
        super("Платеж не найден: " + id);
    }
}
