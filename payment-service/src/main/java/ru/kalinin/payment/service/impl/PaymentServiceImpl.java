package ru.kalinin.payment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.common.exception.payments.PaymentNotFoundException;
import ru.kalinin.common.exception.payments.PaymentUserNotEqualsException;
import ru.kalinin.payment.dto.mapper.PaymentMapper;
import ru.kalinin.payment.dto.response.PaymentResponse;
import ru.kalinin.payment.entity.Payment;
import ru.kalinin.payment.entity.enums.PaymentStatus;
import ru.kalinin.payment.repository.PaymentRepository;
import ru.kalinin.payment.service.interfaces.PaymentService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    // todo ожидание оплаты - ?

    /*
        симуляция успешной оплаты
     */
    @Override
    public PaymentResponse confirm(String username, UUID bookingNumber) {
        Payment payment = getByBookingNumber(bookingNumber);

        if (!Objects.equals(username, payment.getUsername()))
            throw new PaymentUserNotEqualsException(username, bookingNumber);

        payment.setStatus(PaymentStatus.CONFIRMED);

        return paymentMapper.toResponse(payment);
    }

    /*
        симуляция ошибки при оплате
        симуляция отмены оплаты
    */
    @Override
    public PaymentResponse cancel(String username, UUID bookingNumber) {
        Payment payment = getByBookingNumber(bookingNumber);

        if (!Objects.equals(username, payment.getUsername()))
            throw new PaymentUserNotEqualsException(username, bookingNumber);

        payment.setStatus(PaymentStatus.CANCELED);

        return paymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getUserHistory(String username) {
        return paymentMapper.toResponse(
                paymentRepository.findPaymentsByUsername(username)
        );
    }

    @Override
    public Payment getById(Long id) {
        return paymentRepository.findById(id).orElseThrow(
                () -> new PaymentNotFoundException(id)
        );
    }

    @Override
    public Payment getByBookingNumber(UUID bookingNumber) {
        return paymentRepository.getPaymentByBookingNumber(bookingNumber).orElseThrow(
                () -> new PaymentNotFoundException(bookingNumber)
        );
    }
}
