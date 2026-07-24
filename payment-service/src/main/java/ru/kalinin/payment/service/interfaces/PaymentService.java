package ru.kalinin.payment.service.interfaces;

import ru.kalinin.payment.dto.response.PaymentResponse;
import ru.kalinin.payment.entity.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {
    void create(String username, UUID bookingNumber, BigDecimal price);

    PaymentResponse confirm(String username, UUID bookingNumber);

    PaymentResponse cancel(String username, UUID bookingNumber);

    void checkExpiredPayments();

    List<PaymentResponse> getUserHistory(String username);

    Payment getById(Long id);

    Payment getByBookingNumber(UUID bookingNumber);
}
