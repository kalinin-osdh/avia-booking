package ru.kalinin.payment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.common.exception.payments.PaymentAlreadyExistsException;
import ru.kalinin.common.exception.payments.PaymentNotFoundException;
import ru.kalinin.common.exception.payments.PaymentUserNotEqualsException;
import ru.kalinin.common.kafka.event.EventMetaData;
import ru.kalinin.common.kafka.event.payment.PaymentFailedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentSuccessfulEvent;
import ru.kalinin.payment.dto.mapper.PaymentMapper;
import ru.kalinin.payment.dto.response.PaymentResponse;
import ru.kalinin.payment.entity.Payment;
import ru.kalinin.payment.entity.enums.PaymentStatus;
import ru.kalinin.payment.kafka.PaymentProducer;
import ru.kalinin.payment.repository.PaymentRepository;
import ru.kalinin.payment.service.interfaces.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentProducer paymentProducer;
    // todo ожидание оплаты - ?

    @Override
    public void create(String username, UUID bookingNumber, BigDecimal price) {
        Payment payment = paymentMapper.toEntity(username, bookingNumber, price);

        try {
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException exception) {
            throw new PaymentAlreadyExistsException(bookingNumber);
        }
    }

    /*
        симуляция успешной оплаты
     */
    @Override
    public PaymentResponse confirm(String username, UUID bookingNumber) {
        Payment payment = getByBookingNumber(bookingNumber);

        if (!Objects.equals(username, payment.getUsername()))
            throw new PaymentUserNotEqualsException(username, bookingNumber);

        payment.setStatus(PaymentStatus.SUCCESS);

        PaymentSuccessfulEvent event = new PaymentSuccessfulEvent(
                new EventMetaData(
                        UUID.randomUUID(),
                        LocalDateTime.now()
                ),
                payment.getBookingNumber(),
                payment.getPaymentNumber(),
                payment.getUsername(),
                payment.getPrice()
        );

        paymentProducer.sendPaymentSuccess(event);

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

        payment.setStatus(PaymentStatus.FAILED);

        PaymentFailedEvent event = new PaymentFailedEvent(
                new EventMetaData(
                        UUID.randomUUID(),
                        LocalDateTime.now()
                ),
                payment.getBookingNumber(),
                payment.getPaymentNumber(),
                payment.getUsername(),
                payment.getPrice(),
                "Ошибка при оплате."
        );

        paymentProducer.sendPaymentFailed(event);

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getUserHistory(String username) {
        return paymentMapper.toResponse(
                paymentRepository.findPaymentsByUsername(username)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getById(Long id) {
        return paymentRepository.findById(id).orElseThrow(
                () -> new PaymentNotFoundException(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getByBookingNumber(UUID bookingNumber) {
        return paymentRepository.getPaymentByBookingNumber(bookingNumber).orElseThrow(
                () -> new PaymentNotFoundException(bookingNumber)
        );
    }
}
