package ru.kalinin.payment.dto.mapper;

import org.springframework.stereotype.Component;
import ru.kalinin.payment.dto.response.PaymentResponse;
import ru.kalinin.payment.entity.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    public Payment toEntity(String username, UUID bookingNumber, BigDecimal price) {
        return Payment.builder()
                .bookingNumber(bookingNumber)
                .price(price)
                .username(username)
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

    public List<PaymentResponse> toResponse(List<Payment> payment){
        return payment.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
