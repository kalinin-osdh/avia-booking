package ru.kalinin.payment.dto.mapper;

import org.springframework.stereotype.Component;
import ru.kalinin.payment.dto.response.PaymentResponse;
import ru.kalinin.payment.entity.Payment;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentMapper {

    public Payment toEntity(UUID bookingNumber, BigDecimal price) {
        return Payment.builder()
                .bookingNumber(bookingNumber)
                .price(price)
                .build();
    }

    public PaymentResponse toResponse(Payment payment){
        return PaymentResponse.builder()
                .paymentNumber(payment.getPaymentNumber())
                .bookingNumber(payment.getBookingNumber())
                .price(payment.getPrice())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
