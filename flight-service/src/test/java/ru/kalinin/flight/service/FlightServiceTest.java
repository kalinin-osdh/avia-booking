package ru.kalinin.flight.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.kalinin.common.exception.flights.FlightNotFoundException;
import ru.kalinin.common.exception.seats.SeatAlreadyStatusException;
import ru.kalinin.common.exception.seats.SeatNotFoundException;
import ru.kalinin.flight.dto.mapper.FlightMapper;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.response.FlightWithOutSeatsResponse;
import ru.kalinin.flight.dto.response.FlightWithSeatsResponse;
import ru.kalinin.flight.dto.response.PageResponse;
import ru.kalinin.flight.dto.response.SeatCountsResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.Seat;
import ru.kalinin.flight.entity.enums.SeatStatus;
import ru.kalinin.flight.repository.FlightRepository;
import ru.kalinin.flight.service.impl.FlightServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("flight-service")
public class FlightServiceTest {
    @Mock
    private FlightRepository flightRepository;
    @Mock
    private FlightMapper flightMapper;
    @Mock
    private FlightCacheService cacheService;

    @InjectMocks
    private FlightServiceImpl flightService;

    private Flight flight;

    @BeforeEach
    void setUpTestData() {
        flight = Flight.builder()
                .id(1L)
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(2))
                .seats(List.of())
                .build();
    }

    @Test
    @DisplayName("Успешное получение всех полетов - пагинация")
    void shouldFindAllFlights() {
        FlightPageRequest request = new FlightPageRequest();

        FlightWithOutSeatsResponse flightResponse = FlightWithOutSeatsResponse.builder()
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(2))
                .build();

        Page<Flight> page = new PageImpl<>(List.of(flight));

        PageResponse<FlightWithOutSeatsResponse> expectedPageResponse = PageResponse.<FlightWithOutSeatsResponse>builder()
                .content(List.of(flightResponse))
                .page(0)
                .size(5)
                .totalPages(1)
                .totalElements(1)
                .build();

        when(flightRepository.findAllWithFilter(
                isNull(), isNull(), any(Pageable.class)
        )).thenReturn(page);
        when(flightMapper.toPageResponse(any(Page.class)))
                .thenReturn(expectedPageResponse);

        PageResponse<FlightWithOutSeatsResponse> actualPageResponse = flightService
                .findAll(request);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(flightRepository, times(1)).findAllWithFilter(isNull(), isNull(), pageableCaptor.capture());
        verify(flightMapper).toPageResponse(any(Page.class));

        assertEquals(flightResponse.getFlightNumber(), actualPageResponse.getContent().get(0).getFlightNumber());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("flightNumber").getDirection());
    }

    @Test
    @DisplayName("Успешное получение полета по его номеру")
    void shouldFindByFlightNumber() {
        FlightWithSeatsResponse expectedResponse = FlightWithSeatsResponse.builder()
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(2))
                .totalSeats(0L)
                .availableSeats(0L)
                .seats(List.of())
                .build();

        SeatCountsResponse counts = mock(SeatCountsResponse.class);

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));
        when(flightRepository.findCountSeats(1L)).thenReturn(counts);
        when(flightMapper.toFlightWithSeatsResponse(
                flight, counts)
        ).thenReturn(expectedResponse);

        FlightWithSeatsResponse actualResponse = flightService.findByFlightNumber("1A", null);

        assertEquals(expectedResponse, actualResponse);

        verify(flightRepository).findByFlightNumber("1A");
        verify(flightRepository).findCountSeats(1L);
        verify(flightMapper).toFlightWithSeatsResponse(flight, counts);
    }

    @Test
    @DisplayName("Успешное получение полета по его номеру с фильтрацией мест по статусу")
    void shouldFindByFlightNumberWithStatusFilter() {
        FlightWithSeatsResponse expectedResponse = FlightWithSeatsResponse.builder()
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(2))
                .totalSeats(0L)
                .availableSeats(0L)
                .seats(List.of())
                .build();

        Seat available = Seat.builder()
                .status(SeatStatus.AVAILABLE)
                .build();
        Seat sold = Seat.builder()
                .status(SeatStatus.SOLD)
                .build();
        flight.setSeats(List.of(available, sold));

        SeatCountsResponse counts = mock(SeatCountsResponse.class);

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));
        when(flightRepository.findCountSeats(1L)).thenReturn(counts);
        when(flightMapper.toFlightWithSeatsResponse(
                flight, counts)
        ).thenReturn(expectedResponse);

        FlightWithSeatsResponse actualResponse = flightService.findByFlightNumber("1A", SeatStatus.AVAILABLE);

        ArgumentCaptor<Flight> flightCaptor = ArgumentCaptor.forClass(Flight.class);

        verify(flightRepository).findByFlightNumber("1A");
        verify(flightRepository).findCountSeats(1L);
        verify(flightMapper).toFlightWithSeatsResponse(flightCaptor.capture(), eq(counts));

        Flight filteredFlight = flightCaptor.getValue();

        assertEquals(1, filteredFlight.getSeats().size());
        assertEquals(SeatStatus.AVAILABLE, filteredFlight.getSeats().get(0).getStatus());

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Ошибка при получении полета по его номеру - 404 Not Found")
    void shouldThrowFlightNotFoundExceptionWhenFindByFlightNumber() {
        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class,
                () -> flightService.findByFlightNumber("1A", null)
        );

        verify(flightRepository, times(1)).findByFlightNumber("1A");
        verify(flightRepository, never()).findCountSeats(anyLong());
        verifyNoInteractions(flightMapper);
    }

    @Test
    @DisplayName("Успешное изменение статуса места при бронировании")
    void shouldReserveSeat() {
        Seat available = Seat.builder()
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();

        flight.setSeats(List.of(available));

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));

        BigDecimal price = flightService.reserveSeat("1A", "1S");

        assertEquals(BigDecimal.valueOf(1000), price);
        assertEquals(SeatStatus.RESERVED, available.getStatus());

        verify(flightRepository).findByFlightNumber("1A");
        verify(cacheService).evictFlight("1A");
    }

    @Test
    @DisplayName("Ошибка при изменении статуса места (бронирование) - 404 FlightNotFound")
    void shouldThrowFightNotFoundExceptionWhenReserveSeat() {
        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class,
                () -> flightService.reserveSeat("1A", "1S")
        );

        verify(flightRepository, times(1)).findByFlightNumber("1A");
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("Ошибка при изменении статуса места (бронирование) - 404 SeatNotFound")
    void shouldThrowSeatNotFoundExceptionWhenReserveSeat() {
        Seat available = Seat.builder()
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();

        flight.setSeats(List.of(available));

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));

        assertThrows(SeatNotFoundException.class,
                () -> flightService.reserveSeat("1A", "2S")
        );

        verify(flightRepository, times(1)).findByFlightNumber("1A");
        verifyNoInteractions(cacheService);
    }

    @ParameterizedTest
    @EnumSource(
            value = SeatStatus.class,
            names = {"RESERVED", "SOLD"}
    )
    @DisplayName("Ошибка при изменении статуса места (бронирование) - 404 SeatNotFound")
    void shouldThrowSeatAlreadyStatusExceptionWhenReserveSeat(SeatStatus status) {
        Seat notAvailable = Seat.builder()
                .seatNumber("1S")
                .status(status)
                .price(BigDecimal.valueOf(1000))
                .build();

        flight.setSeats(List.of(notAvailable));

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));

        assertThrows(SeatAlreadyStatusException.class,
                () -> flightService.reserveSeat("1A", "1S")
        );

        verify(flightRepository, times(1)).findByFlightNumber("1A");
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("Успешное изменение статуса места при подтверждении оплаты бронирования")
    void shouldSoldSeat() {
        Seat reserved = Seat.builder()
                .seatNumber("1S")
                .status(SeatStatus.RESERVED)
                .price(BigDecimal.valueOf(1000))
                .build();

        flight.setSeats(List.of(reserved));

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));

        flightService.soldSeat("1A", "1S");

        assertEquals(SeatStatus.SOLD, reserved.getStatus());

        verify(flightRepository).findByFlightNumber("1A");
        verify(cacheService).evictFlight("1A");
    }

    @Test
    @DisplayName("Ошибка при изменении статуса места (подтверждение оплаты) - 404 FlightNotFound")
    void shouldThrowFightNotFoundExceptionWhenSoldSeat() {
        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class,
                () -> flightService.soldSeat("1A", "1S")
        );

        verify(flightRepository, times(1)).findByFlightNumber("1A");
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("Ошибка при изменении статуса места (подтверждение оплаты) - 404 SeatNotFound")
    void shouldThrowSeatNotFoundExceptionWhenSoldSeat() {
        Seat reserved = Seat.builder()
                .seatNumber("1S")
                .status(SeatStatus.RESERVED)
                .price(BigDecimal.valueOf(1000))
                .build();

        flight.setSeats(List.of(reserved));

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));

        assertThrows(SeatNotFoundException.class,
                () -> flightService.soldSeat("1A", "2S")
        );

        verify(flightRepository, times(1)).findByFlightNumber("1A");
        verifyNoInteractions(cacheService);
    }

    @ParameterizedTest
    @EnumSource(
            value = SeatStatus.class,
            names = {"AVAILABLE", "SOLD"}
    )
    @DisplayName("Ошибка при изменении статуса места (подтверждение оплаты)- 404 SeatNotFound")
    void shouldThrowSeatAlreadyStatusExceptionWhenSoldSeat(SeatStatus status) {
        Seat notReserved = Seat.builder()
                .seatNumber("1S")
                .status(status)
                .price(BigDecimal.valueOf(1000))
                .build();

        flight.setSeats(List.of(notReserved));

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));

        assertThrows(SeatAlreadyStatusException.class,
                () -> flightService.soldSeat("1A", "1S")
        );

        verify(flightRepository, times(1)).findByFlightNumber("1A");
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("Успешное изменение статуса места при отмене бронирования")
    void shouldAvailableSeat() {
        Seat reserved = Seat.builder()
                .seatNumber("1S")
                .status(SeatStatus.RESERVED)
                .price(BigDecimal.valueOf(1000))
                .build();

        flight.setSeats(List.of(reserved));

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));

        flightService.availableSeat("1A", "1S");

        assertEquals(SeatStatus.AVAILABLE, reserved.getStatus());

        verify(flightRepository).findByFlightNumber("1A");
        verify(cacheService).evictFlight("1A");
    }

    @Test
    @DisplayName("Ошибка при изменении статуса места (отмена бронирования) - 404 FlightNotFound")
    void shouldThrowFightNotFoundExceptionWhenAvailableSeat() {
        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class,
                () -> flightService.availableSeat("1A", "1S")
        );

        verify(flightRepository, times(1)).findByFlightNumber("1A");
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("Ошибка при изменении статуса места (отмена бронирования) - 404 SeatNotFound")
    void shouldThrowSeatNotFoundExceptionWhenAvailableSeat() {
        Seat reserved = Seat.builder()
                .seatNumber("1S")
                .status(SeatStatus.RESERVED)
                .price(BigDecimal.valueOf(1000))
                .build();

        flight.setSeats(List.of(reserved));

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));

        assertThrows(SeatNotFoundException.class,
                () -> flightService.availableSeat("1A", "2S")
        );

        verify(flightRepository, times(1)).findByFlightNumber("1A");
        verifyNoInteractions(cacheService);
    }

    @ParameterizedTest
    @EnumSource(
            value = SeatStatus.class,
            names = {"AVAILABLE", "SOLD"}
    )
    @DisplayName("Ошибка при изменении статуса места (отмена бронирования)- 404 SeatNotFound")
    void shouldThrowSeatAlreadyStatusExceptionWhenAvailableSeat(SeatStatus status) {
        Seat notReserved = Seat.builder()
                .seatNumber("1S")
                .status(status)
                .price(BigDecimal.valueOf(1000))
                .build();

        flight.setSeats(List.of(notReserved));

        when(flightRepository.findByFlightNumber("1A")).thenReturn(Optional.of(flight));

        assertThrows(SeatAlreadyStatusException.class,
                () -> flightService.availableSeat("1A", "1S")
        );

        verify(flightRepository, times(1)).findByFlightNumber("1A");
        verifyNoInteractions(cacheService);
    }
}
