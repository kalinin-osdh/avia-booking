package ru.kalinin.flight.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.kalinin.common.exception.flights.FlightExistsException;
import ru.kalinin.common.exception.flights.FlightNotFoundException;
import ru.kalinin.flight.dto.SeatCountsResponseTest;
import ru.kalinin.flight.dto.mapper.FlightMapper;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.request.FlightRequest;
import ru.kalinin.flight.dto.request.FlightUpdateRequest;
import ru.kalinin.flight.dto.response.FlightAdminPageResponse;
import ru.kalinin.flight.dto.response.FlightAdminResponse;
import ru.kalinin.flight.dto.response.PageResponse;
import ru.kalinin.flight.dto.response.SeatCountsResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.repository.FlightRepository;
import ru.kalinin.flight.service.impl.AdminFlightServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("admin-flight-service")
class AdminFlightServiceTest {
    @Mock
    private FlightRepository flightRepository;
    @Mock
    private FlightMapper flightMapper;
    @Mock
    private FlightCacheService cacheService;

    @InjectMocks
    private AdminFlightServiceImpl service;

    private Flight flight;

    private static LocalDateTime departureTime = LocalDateTime.of(2030, 12, 30, 12, 25);
    private static LocalDateTime arrivalTime = LocalDateTime.of(2030, 12, 30, 15, 45);

    @BeforeEach
    void setUpTestData() {
        flight = Flight.builder()
                .id(1L)
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .seats(List.of())
                .build();
    }


    @Test
    @DisplayName("Успешное получение всех полетов - пагинация")
    void shouldFindAllFlights() {
        FlightPageRequest request = FlightPageRequest.builder()
                .sortBy("id")
                .build();

        FlightAdminPageResponse response = FlightAdminPageResponse.builder()
                .id(1L)
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .seats(List.of())
                .build();

        PageResponse<FlightAdminPageResponse> excpectedPageResponse = PageResponse.<FlightAdminPageResponse>builder()
                .content(List.of(response))
                .page(0)
                .size(5)
                .totalPages(1)
                .totalElements(1)
                .build();

        Page<Flight> page = new PageImpl<>(List.of(flight));

        when(flightRepository.findAllWithFilter(
                isNull(), isNull(), any(Pageable.class)
        )).thenReturn(page);
        when(flightMapper.toAdminPageResponse(any(Page.class))).thenReturn(excpectedPageResponse);

        PageResponse<FlightAdminPageResponse> actualPageResponse = service.findAll(request);

        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(flightRepository).findAllWithFilter(
                isNull(), isNull(), pageableArgumentCaptor.capture()
        );
        verify(flightMapper).toAdminPageResponse(page);

        assertEquals(response.getFlightNumber(), actualPageResponse.getContent().get(0).getFlightNumber());

        Pageable pageable = pageableArgumentCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
    }

    @Test
    @DisplayName("Успешное создание полета")
    void shouldCreateFlight() {
        FlightRequest request = FlightRequest.builder()
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .build();

        SeatCountsResponse countsResponse = new SeatCountsResponseTest(0L, 0L);

        FlightAdminResponse expectedResponse = FlightAdminResponse.builder()
                .id(1L)
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .totalSeats(0L)
                .availableSeats(0L)
                .seats(List.of())
                .build();

        when(flightRepository.existsFlightByFlightNumber("1A")).thenReturn(false);
        when(flightMapper.toEntity(request)).thenReturn(flight);
        when(flightRepository.save(flight)).thenReturn(flight);
        when(flightRepository.findCountSeats(flight.getId())).thenReturn(countsResponse);
        when(flightMapper.toFlightAdminResponse(flight, countsResponse)).thenReturn(expectedResponse);

        FlightAdminResponse actualResponse = service.create(request);

        assertEquals(expectedResponse, actualResponse);

        verify(flightRepository).existsFlightByFlightNumber("1A");
        verify(flightMapper).toEntity(request);
        verify(flightRepository).save(flight);
        verify(flightRepository).findCountSeats(flight.getId());
        verify(flightMapper).toFlightAdminResponse(flight, countsResponse);
    }

    @Test
    @DisplayName("Ошибка при создании полета (полет с таким номером уже существует) - 400 BadRequest")
    void shouldThrowFlightExistsExceptionWhenCreateFlight() {
        FlightRequest request = FlightRequest.builder()
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .build();

        when(flightRepository.existsFlightByFlightNumber("1A")).thenReturn(true);

        assertThrows(FlightExistsException.class,
                () -> service.create(request)
        );

        verify(flightRepository, times(1)).existsFlightByFlightNumber("1A");
        verify(flightRepository, never()).save(any(Flight.class));
        verify(flightRepository, never()).findCountSeats(anyLong());
        verifyNoInteractions(flightMapper);
    }

    @Test
    @DisplayName("Успешное обновление данных полета")
    void shouldUpdateFlight() {
        LocalDateTime newArrivalTime = LocalDateTime.of(2030, 12, 30, 23, 45);

        FlightUpdateRequest request = FlightUpdateRequest.builder()
                .departureCity("MOSCOW")
                .arrivalCity("VLADIVOSTOK")
                .departureTime(departureTime)
                .arrivalTime(newArrivalTime)
                .build();

        FlightAdminResponse expectedResponse = FlightAdminResponse.builder()
                .id(1L)
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("VLADIVOSTOK")
                .departureTime(departureTime)
                .arrivalTime(newArrivalTime)
                .totalSeats(0L)
                .availableSeats(0L)
                .seats(List.of())
                .build();

        SeatCountsResponse countsResponse = new SeatCountsResponseTest(expectedResponse.getTotalSeats(), expectedResponse.getAvailableSeats());

        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.save(flight)).thenReturn(flight);
        when(flightRepository.findCountSeats(1L)).thenReturn(countsResponse);
        when(flightMapper.toFlightAdminResponse(flight, countsResponse)).thenReturn(expectedResponse);

        FlightAdminResponse actualResponse = service.update(1L, request);

        assertEquals(expectedResponse, actualResponse);

        verify(flightRepository).findById(1L);
        verify(flightRepository).save(flight);
        verify(cacheService).evictFlight(flight.getFlightNumber());
        verify(flightRepository).findCountSeats(1L);
        verify(flightMapper).toFlightAdminResponse(flight, countsResponse);
    }

    @Test
    @DisplayName("Ошибка при обновлении данных полета - 404 NotFound")
    void shouldThrowFlightNotFoundExceptionUpdateFlight() {
        FlightUpdateRequest request = FlightUpdateRequest.builder()
                .departureCity("MOSCOW")
                .arrivalCity("VLADIVOSTOK")
                .departureTime(departureTime)
                .arrivalTime(LocalDateTime.of(2030, 12, 30, 23, 45))
                .build();

        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class,
                () -> service.update(1L, request)
        );

        verify(flightRepository).findById(1L);
        verify(flightRepository, never()).save(any(Flight.class));
        verify(flightRepository, never()).findCountSeats(anyLong());
        verifyNoInteractions(cacheService);
        verifyNoInteractions(flightMapper);
    }

    @Test
    @DisplayName("")
    void shouldDeleteFlight() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        service.delete(1L);

        verify(flightRepository).findById(1L);
        verify(flightRepository).delete(flight);
        verify(cacheService).evictFlight(flight.getFlightNumber());
    }

    @Test
    @DisplayName("Ошибка при удалении полета - 404 NotFound")
    void shouldThrowFlightNotFoundExceptionDeleteFlight() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class,
                () -> service.delete(1L)
        );

        verify(flightRepository).findById(1L);
        verify(flightRepository, never()).delete(any(Flight.class));
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("Успешный поиск полета по ID")
    void shouldFindFlightById() {
        FlightAdminResponse expectedResponse = FlightAdminResponse.builder()
                .id(1L)
                .flightNumber("1A")
                .departureCity("MOSCOW")
                .arrivalCity("SOCHI")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .totalSeats(0L)
                .availableSeats(0L)
                .seats(List.of())
                .build();

        SeatCountsResponse countsResponse = new SeatCountsResponseTest(expectedResponse.getTotalSeats(), expectedResponse.getAvailableSeats());

        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.findCountSeats(1L)).thenReturn(countsResponse);
        when(flightMapper.toFlightAdminResponse(flight, countsResponse)).thenReturn(expectedResponse);

        FlightAdminResponse actualResponse = service.findById(1L);

        assertEquals(expectedResponse, actualResponse);

        verify(flightRepository).findById(1L);
        verify(flightRepository).findCountSeats(1L);
        verify(flightMapper).toFlightAdminResponse(flight, countsResponse);
    }

    @Test
    @DisplayName("Ошибка при поиске полета по ID - 404 NotFound")
    void shouldThrowFlightNotFoundExceptionFindFlightById() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class,
                () -> service.findById(1L)
        );

        verify(flightRepository).findById(1L);
        verify(flightRepository, never()).findCountSeats(anyLong());
        verifyNoInteractions(flightMapper);
    }

    @Test
    @DisplayName("Успешное получение полета (entity from database) по ID")
    void shouldGetFlightById() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        Flight actualFlight = service.getById(1L);

        assertEquals(flight, actualFlight);

        verify(flightRepository).findById(1L);
    }

    @Test
    @DisplayName("Ошибка при получении полета (entity from database) по ID - 404 NotFound")
    void shouldThrowFlightNotFoundExceptionWhenGetFlightById() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class,
                () -> service.getById(1L)
        );

        verify(flightRepository).findById(1L);
    }
}