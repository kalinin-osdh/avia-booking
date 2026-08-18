package ru.kalinin.payment.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kalinin.common.kafka.event.payment.PaymentCreatedEvent;
import ru.kalinin.payment.service.interfaces.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("payment-consumer")
public class PaymentConsumerTest {
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentConsumer paymentConsumer;

    @Test
    @DisplayName("Должен создать новый платеж")
    void shouldCreatePayment() {
        PaymentCreatedEvent event = PaymentCreatedEvent.of(
                UUID.randomUUID(),
                "kalinin",
                BigDecimal.valueOf(721.12)
        );

        paymentConsumer.paymentCreateListener(event);

        verify(paymentService).create(event.username(), event.bookingNumber(), event.price());
    }
}
