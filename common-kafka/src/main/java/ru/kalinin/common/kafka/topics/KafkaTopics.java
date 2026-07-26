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

    // создание записи о платеже
    public static final String PAYMENT_CREATED = "payment.created";

    // платеж успешно выполнен
    public static final String PAYMENT_SUCCESSFUL = "payment.successful";

    // платеж НЕ выполнен
    public static final String PAYMENT_FAILED = "payment.failed";

    // бронь оплачена
    public static final String BOOKING_PAYMENT_SUCCESSFUL = "booking.payment.successful";

    // ошибка при оплате брони
    public static final String BOOKING_PAYMENT_FAILED = "booking.payment.failed";
}