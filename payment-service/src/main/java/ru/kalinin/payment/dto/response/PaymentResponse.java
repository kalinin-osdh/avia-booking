package ru.kalinin.payment.dto.response;

import lombok.Builder;
import lombok.Data;
import ru.kalinin.payment.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID paymentNumber;
    private UUID bookingNumber;
    private BigDecimal price;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}
