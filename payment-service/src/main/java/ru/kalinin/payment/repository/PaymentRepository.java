package ru.kalinin.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kalinin.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
