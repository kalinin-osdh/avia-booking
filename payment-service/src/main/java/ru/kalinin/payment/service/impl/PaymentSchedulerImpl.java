package ru.kalinin.payment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.kalinin.payment.service.interfaces.PaymentScheduler;
import ru.kalinin.payment.service.interfaces.PaymentService;

@Service
@RequiredArgsConstructor
public class PaymentSchedulerImpl implements PaymentScheduler {
    private final PaymentService paymentService;

    @Override
    @Scheduled(fixedDelay = 60_000)
    public void checkExpiredPayments() {
        paymentService.checkExpiredPayments();
    }
}
