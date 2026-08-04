package ru.kalinin.flight.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kalinin.common.exception.flights.FlightNotFoundException;
import ru.kalinin.common.exception.seats.SeatNotFoundException;
import ru.kalinin.flight.dto.mapper.SeatMapper;
import ru.kalinin.flight.dto.request.SeatRequest;
import ru.kalinin.flight.dto.response.SeatAdminResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.Seat;
import ru.kalinin.flight.entity.enums.SeatStatus;
import ru.kalinin.flight.repository.SeatRepository;
import ru.kalinin.flight.service.impl.AdminSeatServiceImpl;
import ru.kalinin.flight.service.interfaces.AdminFlightService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("admin-seat-service")
class AdminSeatServiceTest {
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private AdminFlightService adminFlightService;
    @Mock
    private SeatMapper seatMapper;
    @Mock
    private FlightCacheService cacheService;

    @InjectMocks
    private AdminSeatServiceImpl adminSeatService;

    private Seat seat;

    private Flight flight;

    @BeforeEach
    void setUpTestData() {
        flight = Flight.builder()
                .id(1L)
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(LocalDateTime.of(2030, 12, 30, 12, 25))
                .arrivalTime(LocalDateTime.of(2030, 12, 30, 15, 45))
                .build();

        seat = Seat.builder()
                .id(1L)
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    @DisplayName("Успешное получение всех мест полета по его номеру")
    void shouldFindSeatByFlightId() {
        List<Seat> seats = List.of(seat);

        SeatAdminResponse expectedResponse = SeatAdminResponse.builder()
                .id(1L)
                .flightId(1L)
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();

        List<SeatAdminResponse> expectedListResponse = List.of(expectedResponse);

        when(adminFlightService.getById(1L)).thenReturn(flight);
        when(seatRepository.findSeatsByFlight(flight)).thenReturn(seats);
        when(seatMapper.toSeatAdminResponse(seats)).thenReturn(expectedListResponse);

        List<SeatAdminResponse> actualListResponse = adminSeatService.findByFlightId(1L);

        assertEquals(actualListResponse, expectedListResponse);

        verify(adminFlightService).getById(1L);
        verify(seatRepository).findSeatsByFlight(flight);
        verify(seatMapper).toSeatAdminResponse(seats);
    }

    @Test
    @DisplayName("Ошибка при получении всех мест полета по его номеру - 404 FlightNotFound")
    void shouldFlightNotFoundExceptionWhenFindSeatByFlightId() {
        when(adminFlightService.getById(1L)).thenThrow(
                new FlightNotFoundException(1L)
        );

        assertThrows(FlightNotFoundException.class,
                () -> adminSeatService.findByFlightId(1L)
        );

        verify(adminFlightService).getById(1L);
        verifyNoInteractions(seatRepository, seatMapper);
    }

    @Test
    @DisplayName("Успешное создание места")
    void shouldCreateSeat() {
        SeatRequest request = SeatRequest.builder()
                .flightId(1L)
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();

        SeatAdminResponse expectedResponse = SeatAdminResponse.builder()
                .id(1L)
                .flightId(1L)
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();

        when(adminFlightService.getById(1L)).thenReturn(flight);
        when(seatMapper.toEntity(request)).thenReturn(seat);
        seat.setFlight(flight);
        when(seatRepository.save(seat)).thenReturn(seat);
        when(seatMapper.toSeatAdminResponse(seat)).thenReturn(expectedResponse);

        SeatAdminResponse actualResponse = adminSeatService.create(request);

        assertEquals(expectedResponse, actualResponse);

        verify(adminFlightService).getById(1L);
        verify(seatMapper).toEntity(request);
        verify(seatRepository).save(seat);
        verify(cacheService).evictFlight(seat.getFlight().getFlightNumber());
        verify(seatMapper).toSeatAdminResponse(seat);
    }

    @Test
    @DisplayName("Ошибка при создании места - 404 FlightNotFound")
    void shouldThrowFlightNotFoundExceptionWhenCreateSeat() {
        SeatRequest request = SeatRequest.builder()
                .flightId(1L)
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();

        when(adminFlightService.getById(1L)).thenThrow(
                new FlightNotFoundException(1L)
        );

        assertThrows(FlightNotFoundException.class,
                () -> adminSeatService.create(request)
        );

        verify(adminFlightService).getById(1L);
        verifyNoInteractions(seatRepository, seatMapper, cacheService);
    }

    @Test
    @DisplayName("")
    void shouldUpdateSeat() {
        seat.setFlight(flight);
        String oldNum = seat.getFlight().getFlightNumber();

        SeatRequest request = SeatRequest.builder()
                .flightId(1L)
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();

        SeatAdminResponse expectedResponse = SeatAdminResponse.builder()
                .id(1L)
                .flightId(1L)
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();

        when(seatRepository.findByIdWithFlight(1L)).thenReturn(Optional.of(seat));
        when(adminFlightService.getById(1L)).thenReturn(flight);
        when(seatRepository.save(seat)).thenReturn(seat);
        when(seatMapper.toSeatAdminResponse(seat)).thenReturn(expectedResponse);

        SeatAdminResponse actualResponse = adminSeatService.update(1L, request);

        assertEquals(expectedResponse, actualResponse);

        verify(seatRepository).findByIdWithFlight(1L);
        verify(adminFlightService).getById(1L);
        verify(seatRepository).save(seat);
        verify(cacheService).evictFlights(oldNum, flight.getFlightNumber());
        verify(seatMapper).toSeatAdminResponse(seat);
    }

    @Test
    @DisplayName("")
    void shouldThrowSeatNotFoundExceptionWhenUpdateSeat() {
        SeatRequest request = SeatRequest.builder()
                .flightId(1L)
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();

        when(seatRepository.findByIdWithFlight(1L)).thenReturn(Optional.empty());

        assertThrows(SeatNotFoundException.class,
                () -> adminSeatService.update(1L, request)
        );

        verify(seatRepository).findByIdWithFlight(1L);
        verify(seatRepository, never()).save(any(Seat.class));
        verifyNoInteractions(adminFlightService, seatMapper, cacheService);
    }

    @Test
    @DisplayName("")
    void shouldThrowFlightNotFoundExceptionWhenUpdateSeat() {
        seat.setFlight(flight);

        SeatRequest request = SeatRequest.builder()
                .flightId(1L)
                .seatNumber("1S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(1000))
                .build();

        when(seatRepository.findByIdWithFlight(1L)).thenReturn(Optional.of(seat));
        when(adminFlightService.getById(1L)).thenThrow(
                new FlightNotFoundException(1L)
        );

        assertThrows(FlightNotFoundException.class,
                () -> adminSeatService.update(1L, request)
        );

        verify(seatRepository).findByIdWithFlight(1L);
        verify(adminFlightService).getById(1L);
        verify(seatRepository, never()).save(any(Seat.class));
        verifyNoInteractions(seatMapper, cacheService);
    }

    @Test
    @DisplayName("")
    void shouldDeleteSeat() {
        seat.setFlight(flight);

        when(seatRepository.findByIdWithFlight(1L)).thenReturn(Optional.of(seat));

        adminSeatService.delete(1L);

        verify(seatRepository).findByIdWithFlight(1L);
        verify(seatRepository).delete(seat);
        verify(cacheService).evictFlight(seat.getFlight().getFlightNumber());
    }

    @Test
    @DisplayName("")
    void shouldThrowSeatNotFoundExceptionWhenDeleteSeat() {
        when(seatRepository.findByIdWithFlight(1L)).thenReturn(Optional.empty());

        assertThrows(SeatNotFoundException.class,
                () -> adminSeatService.delete(1L)
        );

        verify(seatRepository).findByIdWithFlight(1L);
        verify(seatRepository, never()).delete(any(Seat.class));
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("")
    void shouldGetSeatById() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        Seat actualSeat = adminSeatService.getById(1L);

        assertEquals(seat, actualSeat);

        verify(seatRepository).findById(1L);
    }

    @Test
    @DisplayName("")
    void shouldThrowSeatNotFoundExceptionGetSeatById() {
        when(seatRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SeatNotFoundException.class,
                () -> adminSeatService.getById(1L)
        );

        verify(seatRepository).findById(1L);
    }

    @Test
    @DisplayName("")
    void shouldGetSeatByIdWithFlight() {
        when(seatRepository.findByIdWithFlight(1L)).thenReturn(Optional.of(seat));

        Seat actualSeat = adminSeatService.getByIdWithFlight(1L);

        assertEquals(seat, actualSeat);

        verify(seatRepository).findByIdWithFlight(1L);
    }

    @Test
    @DisplayName("")
    void shouldThrowSeatNotFoundExceptionWhenGetSeatByIdWithFlight() {
        when(seatRepository.findByIdWithFlight(1L)).thenReturn(Optional.empty());

        assertThrows(SeatNotFoundException.class,
                () -> adminSeatService.getByIdWithFlight(1L)
        );

        verify(seatRepository).findByIdWithFlight(1L);
    }
}