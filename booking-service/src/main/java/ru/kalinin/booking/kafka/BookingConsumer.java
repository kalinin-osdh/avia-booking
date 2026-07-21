package ru.kalinin.booking.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.kalinin.booking.entity.enums.BookingStatus;
import ru.kalinin.booking.service.interfaces.BookingService;
import ru.kalinin.common.kafka.event.seat.SeatReservedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservationFailedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;

@Service
@RequiredArgsConstructor
public class BookingConsumer {
    private final BookingService bookingService;

    @KafkaListener(
            topics = KafkaTopics.SEAT_RESERVED
    )
    public void seatReservedListener(SeatReservedEvent event){
        bookingService.confirmBooking(event.bookingId());
    }

    @KafkaListener(
            topics = KafkaTopics.SEAT_RESERVATION_FAILED
    )
    public void seatReservationFailedListener(SeatReservationFailedEvent event){
        bookingService.declineBooking(event.bookingId());
    }
}

// todo добавить ОЖИДАНИЕ ОПЛАТЫ?

// todo обработать try { } catch () { отправить в notification service уведомление об ошибке пользователю ?? }