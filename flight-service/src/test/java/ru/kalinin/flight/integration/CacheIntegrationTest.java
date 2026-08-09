package ru.kalinin.flight.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.response.FlightWithOutSeatsResponse;
import ru.kalinin.flight.dto.response.FlightWithSeatsResponse;
import ru.kalinin.flight.dto.response.PageResponse;
import ru.kalinin.flight.dto.response.SeatResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.enums.SeatStatus;
import ru.kalinin.flight.repository.FlightRepository;
import ru.kalinin.flight.factory.TestDataFactory;
import ru.kalinin.flight.service.interfaces.AdminFlightService;
import ru.kalinin.flight.service.interfaces.FlightService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Tag("cache-integration")
public class CacheIntegrationTest {
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private FlightService flightService;

    @Autowired
    private AdminFlightService adminFlightService;

    @MockitoSpyBean
    private FlightRepository flightRepository;

    private static final String FLIGHT_BY_NUMBER_CACHE = "flightByNumber";
    private static final String FLIGHT_PAGE_CACHE = "flightPage";

    private Flight flight;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("flight_db_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void clearCache() {
        flight = TestDataFactory.createFlight("1A");
        flight.setSeats(List.of(
                TestDataFactory.createSeat(flight, "1S", SeatStatus.AVAILABLE),
                TestDataFactory.createSeat(flight, "2S", SeatStatus.RESERVED)
        ));
        flightRepository.save(flight);
    }

    @AfterEach
    void clearCacheAndData() {
        cacheManager.getCacheNames().forEach(
                name -> cacheManager.getCache(name).clear()
        );

        flightRepository.deleteAll();
    }

    @Test
    @DisplayName("Должен сохранить flight в кеш")
    void shouldCacheFlightByNumber() {
        flightService.findByFlightNumber(flight.getFlightNumber(), null);
        flightService.findByFlightNumber(flight.getFlightNumber(), SeatStatus.AVAILABLE);

        Cache cache = cacheManager.getCache(FLIGHT_BY_NUMBER_CACHE);
        assertThat(cache).isNotNull();

        FlightWithSeatsResponse actualFlightCache = cache.get("1A:", FlightWithSeatsResponse.class);
        FlightWithSeatsResponse actualFlightCacheWithAvailableSeats = cache.get("1A:AVAILABLE", FlightWithSeatsResponse.class);

        assertThat(actualFlightCache)
                .isNotNull()
                .satisfies(f -> {
                    assertThat(f.getFlightNumber())
                            .isEqualTo(flight.getFlightNumber());
                    assertThat(f.getSeats())
                            .extracting(SeatResponse::getSeatNumber)
                            .containsExactly("1S", "2S");
                });

        assertThat(actualFlightCacheWithAvailableSeats)
                .isNotNull()
                .satisfies(f -> {
                    assertThat(f.getFlightNumber())
                            .isEqualTo(flight.getFlightNumber());
                    assertThat(f.getSeats())
                            .extracting(SeatResponse::getSeatNumber)
                            .containsExactly("1S");
                });
    }

    @Test
    @DisplayName("Должен возвращать flight из кеша")
    void shouldReturnFlightFromCache() {
        flightService.findByFlightNumber(flight.getFlightNumber(), null);
        flightService.findByFlightNumber(flight.getFlightNumber(), null);

        assertThat(cacheManager.getCache(FLIGHT_BY_NUMBER_CACHE)).isNotNull();

        verify(flightRepository, times(1)).findByFlightNumber(flight.getFlightNumber());

        flightService.findByFlightNumber(flight.getFlightNumber(), SeatStatus.AVAILABLE);
        flightService.findByFlightNumber(flight.getFlightNumber(), SeatStatus.AVAILABLE);

        verify(flightRepository, times(2)).findByFlightNumber(flight.getFlightNumber());
    }

    @Test
    @DisplayName("Должен удалиться flight из кеша после обновления данных о нем")
    void shouldEvictCacheFlightByNumber() {
        flightService.findByFlightNumber(flight.getFlightNumber(), null);
        flightService.findByFlightNumber(flight.getFlightNumber(), SeatStatus.AVAILABLE);

        Cache cache = cacheManager.getCache(FLIGHT_BY_NUMBER_CACHE);

        assertThat(cache)
                .isNotNull()
                .satisfies(c -> {
                    assertThat(c.get("1A:")).isNotNull();
                    assertThat(c.get("1A:AVAILABLE")).isNotNull();
                });

        flightService.reserveSeat(
                flight.getFlightNumber(),
                flight.getSeats().get(0).getSeatNumber()
        );

        assertThat(cache)
                .isNotNull()
                .satisfies(c -> {
                    assertThat(c.get("1A:")).isNull();
                    assertThat(c.get("1A:AVAILABLE")).isNull();
                });
    }

    @Test
    @DisplayName("Должен сохранить страницу в кеш")
    void shouldCacheFlightPage() {
        FlightPageRequest pageRequest = FlightPageRequest.builder()
                .page(0)
                .size(5)
                .sortBy("flightNumber")
                .sortDirection("asc")
                .departureCity(null)
                .arrivalCity("SOCHI")
                .build();
        flightService.findAll(pageRequest);

        Cache cache = cacheManager.getCache(FLIGHT_PAGE_CACHE);

        assertThat(cache).isNotNull();

        PageResponse<FlightWithOutSeatsResponse> actualPage = cache.get("0:5:flightNumber:asc::SOCHI", PageResponse.class);

        assertThat(actualPage).isNotNull();
    }

    @Test
    @DisplayName("Должен очистить кеш всех страниц после обновления данных")
    void shouldEvictCacheFlightPageAllEntries() {
        FlightPageRequest pageRequest = FlightPageRequest.builder()
                .page(0)
                .size(5)
                .sortBy("flightNumber")
                .sortDirection("asc")
                .departureCity(null)
                .arrivalCity("SOCHI")
                .build();
        FlightPageRequest anotherPageRequest = FlightPageRequest.builder().build();
        flightService.findAll(pageRequest);
        flightService.findAll(anotherPageRequest);

        Cache cache = cacheManager.getCache(FLIGHT_PAGE_CACHE);

        assertThat(cache)
                .isNotNull()
                .satisfies(c -> {
                    assertThat(c.get("0:5:flightNumber:asc::SOCHI")).isNotNull();
                    assertThat(c.get("0:5:flightNumber:desc::")).isNotNull();
                });

        adminFlightService.delete(flight.getId());

        assertThat(cache)
                .isNotNull()
                .satisfies(c -> {
                    assertThat(c.get("0:5:flightNumber:asc::SOCHI")).isNull();
                    assertThat(c.get("0:5:flightNumber:desc::")).isNull();
                });
    }

}
