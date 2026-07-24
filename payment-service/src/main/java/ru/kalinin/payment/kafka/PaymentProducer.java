package ru.kalinin.payment.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.kalinin.common.kafka.event.payment.PaymentFailedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentSuccessfulEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;

@Service
@RequiredArgsConstructor
public class PaymentProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentSuccess(PaymentSuccessfulEvent event){
        kafkaTemplate.send(
                KafkaTopics.PAYMENT_SUCCESSFUL,
                event
        );
    }

    public void sendPaymentFailed(PaymentFailedEvent event){
        kafkaTemplate.send(
                KafkaTopics.PAYMENT_FAILED,
                event
        );
    }
}
