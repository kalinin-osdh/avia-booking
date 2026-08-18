package ru.kalinin.payment.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.kalinin.common.kafka.event.payment.PaymentCreatedEvent;
import ru.kalinin.common.kafka.topics.KafkaTopics;
import ru.kalinin.payment.entity.Payment;
import ru.kalinin.payment.entity.enums.PaymentStatus;
import ru.kalinin.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Tag("kafka-payment-integration")
public class KafkaPaymentIntegrationTest {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("payment_db_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @AfterEach
    void cleanUp() {
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Должен отправлять событие в DLT, если платеж уже существует")
    void shouldSendEventToDltWhenPaymentAlreadyExist() throws Exception {
        PaymentCreatedEvent event = PaymentCreatedEvent.of(
                UUID.randomUUID(),
                "kalinin",
                BigDecimal.valueOf(1234.56)
        );

        kafkaTemplate.send(KafkaTopics.PAYMENT_CREATED, event).get();

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<Payment> payment = paymentRepository.getPaymentByBookingNumber(event.bookingNumber());

                    assertThat(payment).isPresent()
                            .get()
                            .returns(event.bookingNumber(), Payment::getBookingNumber)
                            .returns(event.username(), Payment::getUsername)
                            .returns(PaymentStatus.PENDING, Payment::getStatus)
                            .satisfies(
                                    p -> assertThat(p.getPrice()).isEqualByComparingTo(event.price()),
                                    p -> assertThat(p.getCreatedAt()).isBefore(p.getExpiredAt())
                            );
                });
    }

    @Test
    @DisplayName("Не должен повторно создавать платеж если он уже существует")
    void shouldNotCreatePaymentWhenPaymentAlreadyExists() throws Exception {
        PaymentCreatedEvent event = PaymentCreatedEvent.of(
                UUID.randomUUID(),
                "kalinin",
                BigDecimal.valueOf(1234.56)
        );

        PaymentCreatedEvent badEvent = PaymentCreatedEvent.of(
                event.bookingNumber(),
                "someUsername",
                BigDecimal.valueOf(6543.21)
        );

        paymentRepository.save(
                Payment.builder()
                        .bookingNumber(event.bookingNumber())
                        .username(event.username())
                        .price(event.price())
                        .build()
        );

        kafkaTemplate.send(KafkaTopics.PAYMENT_CREATED, badEvent).get();

        try (Consumer<String, PaymentCreatedEvent> dltConsumer = createDltConsumer()) {
            dltConsumer.subscribe(List.of(KafkaTopics.PAYMENT_CREATED + ".DLT"));
            dltConsumer.poll(Duration.ofMillis(500));

            kafkaTemplate.send(KafkaTopics.PAYMENT_CREATED, badEvent).get();

            ConsumerRecord<String, PaymentCreatedEvent> dltRecord =
                    KafkaTestUtils.getSingleRecord(
                            dltConsumer, KafkaTopics.PAYMENT_CREATED + ".DLT"
                    );

            assertThat(dltRecord.value()).isNotNull().isEqualTo(badEvent);
        }
    }

    private Consumer<String, PaymentCreatedEvent> createDltConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers()
        );
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-dlt-group-" + UUID.randomUUID()
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

