package ru.kalinin.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.kalinin.payment.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_number", nullable = false, unique = true)
    @Builder.Default
    private UUID paymentNumber = UUID.randomUUID();

    @Column(name = "booking_number", nullable = false, unique = true)
    private UUID bookingNumber;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expired_at", nullable = false)
    @Builder.Default
    private LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(1);
}
