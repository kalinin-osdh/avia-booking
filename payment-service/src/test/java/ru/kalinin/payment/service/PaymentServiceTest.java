package ru.kalinin.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.kalinin.common.exception.payments.PaymentAlreadyExistsException;
import ru.kalinin.common.exception.payments.PaymentNotFoundException;
import ru.kalinin.common.exception.payments.PaymentUserNotEqualsException;
import ru.kalinin.common.kafka.event.payment.PaymentFailedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentSuccessfulEvent;
import ru.kalinin.payment.dto.mapper.PaymentMapper;
import ru.kalinin.payment.dto.response.PaymentResponse;
import ru.kalinin.payment.entity.Payment;
import ru.kalinin.payment.entity.enums.PaymentStatus;
import ru.kalinin.payment.kafka.PaymentProducer;
import ru.kalinin.payment.repository.PaymentRepository;
import ru.kalinin.payment.service.impl.PaymentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("payment-service")
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PaymentProducer paymentProducer;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .id(1L)
                .username("kalinin")
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Должен создать новый платеж")
    void shouldCreatePayment() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        when(paymentMapper.toEntity("kalinin", bookingNumber, price)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);

        paymentService.create("kalinin", bookingNumber, price);

        verify(paymentMapper).toEntity("kalinin", bookingNumber, price);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Должен вернуть PaymentAlreadyExistsException при повторной попытке создать платеж")
    void shouldThrowPaymentAlreadyExistsExceptionWhenPaymentAlreadyCreated() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        when(paymentMapper.toEntity("kalinin", bookingNumber, price)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenThrow(new DataIntegrityViolationException(""));

        assertThatThrownBy(() -> paymentService.create("kalinin", bookingNumber, price))
                .isInstanceOf(PaymentAlreadyExistsException.class)
                .hasMessageContaining(bookingNumber.toString());

        verify(paymentMapper).toEntity("kalinin", bookingNumber, price);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Должен подтвердить оплату и отправить событие")
    void shouldConfirmPayment() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentNumber(payment.getPaymentNumber())
                .bookingNumber(bookingNumber)
                .price(payment.getPrice())
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.getPaymentByBookingNumber(payment.getBookingNumber()))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse actual = paymentService.confirm("kalinin", bookingNumber);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(actual.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        ArgumentCaptor<PaymentSuccessfulEvent> argumentCaptor = ArgumentCaptor.forClass(PaymentSuccessfulEvent.class);

        verify(paymentProducer).sendPaymentSuccess(argumentCaptor.capture());

        PaymentSuccessfulEvent event = argumentCaptor.getValue();

        assertThat(event).isNotNull()
                .satisfies(e -> assertThat(e.price()).isEqualByComparingTo(price))
                .extracting(
                        PaymentSuccessfulEvent::bookingNumber,
                        PaymentSuccessfulEvent::paymentNumber,
                        PaymentSuccessfulEvent::username
                )
                .containsExactly(
                        payment.getBookingNumber(),
                        payment.getPaymentNumber(),
                        payment.getUsername()
                );

        verify(paymentRepository).getPaymentByBookingNumber(payment.getBookingNumber());
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    @DisplayName("Должен вернуть PaymentNotFoundException если платеж не найден при попытке его подтвердить")
    void shouldThrowPaymentNotFoundExceptionWhenConfirmPayment() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        when(paymentRepository.getPaymentByBookingNumber(payment.getBookingNumber()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirm("kalinin", bookingNumber))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining(bookingNumber.toString());

        verify(paymentRepository).getPaymentByBookingNumber(payment.getBookingNumber());
        verifyNoInteractions(paymentProducer, paymentMapper);
    }

    @Test
    @DisplayName("Должен вернуть PaymentUserNotEqualsException при попытке подтвердить чужой платеж")
    void shouldThrowPaymentUserNotEqualsExceptionWhenConfirmPayment() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        when(paymentRepository.getPaymentByBookingNumber(payment.getBookingNumber()))
                .thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirm("username", bookingNumber))
                .isInstanceOf(PaymentUserNotEqualsException.class)
                .hasMessageContaining("username", bookingNumber.toString());

        verify(paymentRepository).getPaymentByBookingNumber(payment.getBookingNumber());
        verifyNoInteractions(paymentProducer, paymentMapper);
    }

    @Test
    @DisplayName("Должен отменить оплату и отправить событие")
    void shouldCancelPayment() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentNumber(payment.getPaymentNumber())
                .bookingNumber(bookingNumber)
                .price(payment.getPrice())
                .status(PaymentStatus.FAILED)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.getPaymentByBookingNumber(payment.getBookingNumber()))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse actual = paymentService.cancel("kalinin", bookingNumber);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(actual.getStatus()).isEqualTo(PaymentStatus.FAILED);

        ArgumentCaptor<PaymentFailedEvent> argumentCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);

        verify(paymentProducer).sendPaymentFailed(argumentCaptor.capture());

        PaymentFailedEvent event = argumentCaptor.getValue();

        assertThat(event).isNotNull()
                .satisfies(e -> assertThat(e.price()).isEqualByComparingTo(price))
                .extracting(
                        PaymentFailedEvent::bookingNumber,
                        PaymentFailedEvent::paymentNumber,
                        PaymentFailedEvent::username,
                        PaymentFailedEvent::reason
                )
                .containsExactly(
                        payment.getBookingNumber(),
                        payment.getPaymentNumber(),
                        payment.getUsername(),
                        "Ошибка при оплате."
                );

        verify(paymentRepository).getPaymentByBookingNumber(payment.getBookingNumber());
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    @DisplayName("Должен вернуть PaymentNotFoundException если платеж не найден при попытке его отменить")
    void shouldThrowPaymentNotFoundExceptionWhenCancelPayment() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        when(paymentRepository.getPaymentByBookingNumber(payment.getBookingNumber()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.cancel("kalinin", bookingNumber))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining(bookingNumber.toString());

        verify(paymentRepository).getPaymentByBookingNumber(payment.getBookingNumber());
        verifyNoInteractions(paymentProducer, paymentMapper);
    }

    @Test
    @DisplayName("Должен вернуть PaymentUserNotEqualsException при попытке отменить чужой платеж")
    void shouldThrowPaymentUserNotEqualsExceptionWhenCancelPayment() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        when(paymentRepository.getPaymentByBookingNumber(payment.getBookingNumber()))
                .thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.cancel("username", bookingNumber))
                .isInstanceOf(PaymentUserNotEqualsException.class)
                .hasMessageContaining("username", bookingNumber.toString());

        verify(paymentRepository).getPaymentByBookingNumber(payment.getBookingNumber());
        verifyNoInteractions(paymentProducer, paymentMapper);
    }

    @Test
    @DisplayName("Должен отменить просроченные платежи и отправить соответствующие события если они существуют")
    void shouldSendPaymentFailedEventWhenHasExpiredPayments() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        List<Payment> expiredPayments = List.of(payment);

        when(paymentRepository.findAllByStatusAndExpiredAtBefore(eq(PaymentStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(expiredPayments);

        paymentService.checkExpiredPayments();

        ArgumentCaptor<PaymentFailedEvent> argumentCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);

        verify(paymentProducer).sendPaymentFailed(argumentCaptor.capture());

        PaymentFailedEvent event = argumentCaptor.getValue();

        assertThat(event).isNotNull()
                .satisfies(e -> assertThat(e.price()).isEqualByComparingTo(price))
                .extracting(
                        PaymentFailedEvent::bookingNumber,
                        PaymentFailedEvent::paymentNumber,
                        PaymentFailedEvent::username,
                        PaymentFailedEvent::reason
                )
                .containsExactly(
                        payment.getBookingNumber(),
                        payment.getPaymentNumber(),
                        payment.getUsername(),
                        "Время оплаты истекло"
                );

        verify(paymentRepository).findAllByStatusAndExpiredAtBefore(eq(PaymentStatus.PENDING), any(LocalDateTime.class));
        verifyNoMoreInteractions(paymentRepository, paymentProducer);
        verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Не должен отменять просроченные платежи и отправлять соответствующие события если они не существуют")
    void shouldNotSendPaymentFailedEventWhenHasNotExpiredPayments() {
        List<Payment> expiredPayments = List.of();

        when(paymentRepository.findAllByStatusAndExpiredAtBefore(eq(PaymentStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(expiredPayments);

        paymentService.checkExpiredPayments();

        verify(paymentRepository).findAllByStatusAndExpiredAtBefore(eq(PaymentStatus.PENDING), any(LocalDateTime.class));
        verifyNoInteractions(paymentProducer);
        verifyNoInteractions(paymentMapper);
    }


    @Test
    @DisplayName("Должен вернуть историю платежей пользователя")
    void shouldReturnPaymentHistoryByUsername() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        List<Payment> payments = List.of(payment);
        List<PaymentResponse> responses = List.of(
                PaymentResponse.builder()
                        .paymentNumber(payment.getPaymentNumber())
                        .bookingNumber(bookingNumber)
                        .price(price)
                        .status(payment.getStatus())
                        .createdAt(payment.getCreatedAt())
                        .build()
        );

        when(paymentRepository.findPaymentsByUsername("kalinin")).thenReturn(payments);
        when(paymentMapper.toResponse(payments)).thenReturn(responses);

        List<PaymentResponse> actualResponses = paymentService.getUserHistory("kalinin");

        assertThat(actualResponses)
                .isNotEmpty()
                .isEqualTo(responses);

        verify(paymentRepository).findPaymentsByUsername("kalinin");
        verify(paymentMapper).toResponse(payments);
    }

    @Test
    @DisplayName("Должен вернуть пустую историю платежей если платежи отсутствуют")
    void shouldReturnEmptyPaymentHistoryWhenPaymentsNotFound() {
        UUID bookingNumber = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(1750.75);
        payment.setBookingNumber(bookingNumber);
        payment.setPrice(price);

        List<Payment> payments = List.of();
        List<PaymentResponse> responses = List.of();

        when(paymentRepository.findPaymentsByUsername("kalinin")).thenReturn(payments);
        when(paymentMapper.toResponse(payments)).thenReturn(responses);

        List<PaymentResponse> actualResponses = paymentService.getUserHistory("kalinin");

        assertThat(actualResponses)
                .isEmpty();

        verify(paymentRepository).findPaymentsByUsername("kalinin");
        verify(paymentMapper).toResponse(payments);
    }


    @Test
    @DisplayName("Должен вернуть платеж по номеру бронирования")
    void shouldReturnPaymentByBookingNumber() {
        UUID bookingNumber = UUID.randomUUID();
        payment.setBookingNumber(bookingNumber);

        when(paymentRepository.getPaymentByBookingNumber(payment.getBookingNumber()))
                .thenReturn(Optional.of(payment));

        Payment actual = paymentService.getByBookingNumber(bookingNumber);

        assertThat(actual).isNotNull();

        verify(paymentRepository).getPaymentByBookingNumber(bookingNumber);
    }

    @Test
    @DisplayName("Должен вернуть PaymentNotFoundException если платеж не найден по номеру бронирования")
    void shouldThrowPaymentNotFoundExceptionWhenPaymentNotFound() {
        UUID bookingNumber = UUID.randomUUID();

        when(paymentRepository.getPaymentByBookingNumber(bookingNumber))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getByBookingNumber(bookingNumber))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining(bookingNumber.toString());

        verify(paymentRepository).getPaymentByBookingNumber(bookingNumber);
    }
}
