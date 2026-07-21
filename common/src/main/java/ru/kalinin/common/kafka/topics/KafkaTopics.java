package ru.kalinin.common.kafka.topics;


public final class KafkaTopics {

    private KafkaTopics(){

    }

    // бронь создана
    public static final String BOOKING_CREATED = "booking.created";

    // место зарезервировано
    public static final String SEAT_RESERVED = "seat.reserved";

    // ошибка резервации
    public static final String SEAT_RESERVATION_FAILED = "seat.reservation.failed";
}