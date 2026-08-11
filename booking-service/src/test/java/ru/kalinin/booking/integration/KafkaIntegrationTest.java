package ru.kalinin.booking.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import ru.kalinin.booking.entity.Booking;
import ru.kalinin.booking.entity.enums.BookingStatus;
import ru.kalinin.booking.kafka.BookingProducer;
import ru.kalinin.booking.repository.BookingRepository;
import ru.kalinin.common.kafka.event.payment.PaymentCreatedEvent;
import ru.kalinin.common.kafka.event.seat.SeatReservedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Tag("booking-kafka-integration")
public class KafkaIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    private BookingProducer bookingProducer;

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("booking_db_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    private Booking booking;

    @BeforeEach
    void setUpData() {
        booking = Booking.builder()
                .username("kalinin")
                .flightNumber("1A")
                .seatNumber("1S")
                .build();

        bookingRepository.save(booking);
    }

    @AfterEach
    void cleanUp() {
        bookingRepository.deleteAll();
    }

    @Test
    @DisplayName("Должен подтвердить бронирование и отправить событие создания платежа")
    void shouldConfirmBookingAndSendPaymentCreatedEvent() throws Exception {
        assertThat(booking.getPrice()).isNull();

        BigDecimal price = BigDecimal.valueOf(1250);
        SeatReservedEvent event = SeatReservedEvent.of(
                booking.getId(),
                booking.getBookingNumber(),
                booking.getUsername(),
                booking.getFlightNumber(),
                booking.getSeatNumber(),
                price);

        try (Consumer<String, PaymentCreatedEvent> consumer = createConsumer()) {
            consumer.subscribe(List.of(KafkaTopics.PAYMENT_CREATED));
            consumer.poll(Duration.ofMillis(500));

            kafkaTemplate.send(KafkaTopics.SEAT_RESERVED, event).get();

            ConsumerRecord<String, PaymentCreatedEvent> record =
                    KafkaTestUtils.getSingleRecord(consumer, KafkaTopics.PAYMENT_CREATED);

            PaymentCreatedEvent actualEvent = record.value();

            assertThat(actualEvent).isNotNull()
                    .extracting(
                            PaymentCreatedEvent::bookingNumber,
                            PaymentCreatedEvent::username,
                            PaymentCreatedEvent::price
                    )
                    .containsExactly(
                            booking.getBookingNumber(),
                            booking.getUsername(),
                            price
                    );
        }

        Booking actualBooking = bookingRepository.findById(booking.getId()).orElseThrow();

        assertThat(actualBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(actualBooking.getPrice()).isEqualByComparingTo(price);
    }

    @Test
    @DisplayName("должен dlt")
    void someAnotherTest() throws Exception {
        SeatReservedEvent badEvent = SeatReservedEvent.of(
                Long.MAX_VALUE,
                UUID.randomUUID(),
                "petrova",
                "1FFAF",
                "97ASG",
                BigDecimal.valueOf(1250));

        try (Consumer<String, PaymentCreatedEvent> consumer = createConsumer()) {
            consumer.subscribe(List.of(KafkaTopics.PAYMENT_CREATED));
            consumer.poll(Duration.ofMillis(500));

            kafkaTemplate.send(KafkaTopics.SEAT_RESERVED, badEvent).get();

            ConsumerRecords<String, PaymentCreatedEvent> emptyRecord =
                    KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(3));

            assertThat(emptyRecord).isEmpty();
        }
    }


    @Test
    @DisplayName("должен идемпотентность")
    void shouldDontSendPaymentCreatedEventWhenBookingAlreadyConfirmed() throws Exception {
        assertThat(booking.getPrice()).isNull();

        BigDecimal price = BigDecimal.valueOf(1250);
        SeatReservedEvent event = SeatReservedEvent.of(
                booking.getId(),
                booking.getBookingNumber(),
                booking.getUsername(),
                booking.getFlightNumber(),
                booking.getSeatNumber(),
                price);

        try (Consumer<String, PaymentCreatedEvent> consumer = createConsumer()) {
            consumer.subscribe(List.of(KafkaTopics.PAYMENT_CREATED));
            consumer.poll(Duration.ofMillis(500));

            kafkaTemplate.send(KafkaTopics.SEAT_RESERVED, event).get();

            ConsumerRecord<String, PaymentCreatedEvent> firstRecord =
                    KafkaTestUtils.getSingleRecord(consumer, KafkaTopics.PAYMENT_CREATED);

            assertThat(firstRecord).isNotNull();

            Booking actualBooking = bookingRepository.findById(booking.getId()).orElseThrow();

            assertThat(actualBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
            assertThat(actualBooking.getPrice()).isEqualByComparingTo(price);

            kafkaTemplate.send(KafkaTopics.SEAT_RESERVED, event).get();

            ConsumerRecords<String, PaymentCreatedEvent> secondRecord =
                    KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(3));

            assertThat(secondRecord).isEmpty();
        }

        Booking actualBooking = bookingRepository.findById(booking.getId()).orElseThrow();

        assertThat(actualBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(actualBooking.getPrice()).isEqualByComparingTo(price);

        verify(bookingProducer, times(1)).sendPaymentCreated(any(PaymentCreatedEvent.class));
    }

    private Consumer<String, PaymentCreatedEvent> createConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers()
        );
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-consumer-group-" + UUID.randomUUID()
        );
        consumerProps.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        JsonDeserializer<PaymentCreatedEvent> jsonDeserializer
                = new JsonDeserializer<>(PaymentCreatedEvent.class);

        jsonDeserializer.addTrustedPackages("ru.kalinin.common.kafka.event");

        return new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                jsonDeserializer
        ).createConsumer();
    }
}
