package ru.kalinin.payment.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.kalinin.common.kafka.event.payment.PaymentFailedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentSuccessfulEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("payment-producer")
public class PaymentProducerTest {
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentProducer paymentProducer;

    @Test
    @DisplayName("Должен отправить событие Kafka-топик PAYMENT_SUCCESSFUL")
    void shouldSendPaymentSuccess(){
        PaymentSuccessfulEvent event = PaymentSuccessfulEvent.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "kalinin",
                BigDecimal.valueOf(750.99)
        );

        paymentProducer.sendPaymentSuccess(event);

        verify(kafkaTemplate).send(KafkaTopics.PAYMENT_SUCCESSFUL, event);
    }

    @Test
    @DisplayName("Должен отправить событие Kafka-топик PAYMENT_FAILED")
    void shouldSendPaymentFailed(){
        PaymentFailedEvent event = PaymentFailedEvent.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "kalinin",
                BigDecimal.valueOf(750.99),
                "some reason"
        );

        paymentProducer.sendPaymentFailed(event);

        verify(kafkaTemplate).send(KafkaTopics.PAYMENT_FAILED, event);
    }

}
