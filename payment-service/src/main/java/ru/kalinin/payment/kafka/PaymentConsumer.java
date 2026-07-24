package ru.kalinin.payment.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.kalinin.common.exception.payments.PaymentAlreadyExistsException;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;
import ru.kalinin.common.kafka.event.payment.PaymentCreatedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;
import ru.kalinin.payment.service.interfaces.PaymentService;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {
    private final PaymentService paymentService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_CREATED
    )
    public void paymentCreateListener(PaymentCreatedEvent event){
        paymentService.create(event.username(), event.bookingNumber(), event.price());
    }
}
