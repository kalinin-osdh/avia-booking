package ru.kalinin.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kalinin.payment.entity.Payment;
import ru.kalinin.payment.entity.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> getPaymentByBookingNumber(UUID bookingNumber);

    List<Payment> findPaymentsByUsername(String username);

    List<Payment> findAllByStatusAndExpiredAtBefore(PaymentStatus status, LocalDateTime expiredAt);
}
