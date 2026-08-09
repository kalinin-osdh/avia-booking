package ru.kalinin.flight.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.kalinin.common.exception.GlobalExceptionHandler;
import ru.kalinin.common.exception.flights.FlightNotFoundException;
import ru.kalinin.flight.config.FlightTestSecurityConfig;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.response.FlightWithOutSeatsResponse;
import ru.kalinin.flight.dto.response.FlightWithSeatsResponse;
import ru.kalinin.flight.dto.response.PageResponse;
import ru.kalinin.flight.service.interfaces.FlightService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FlightController.class)
@Import({FlightTestSecurityConfig.class, GlobalExceptionHandler.class})
@Tag("controller")
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightService service;

    @Test
    @DisplayName("GET /api/v1/flights - 200 Получить список всех полетов с фильтрами (пагинация)")
    void shouldFindFlights() throws Exception {
        PageResponse<FlightWithOutSeatsResponse> pageResponse =
                PageResponse.<FlightWithOutSeatsResponse>builder()
                        .content(List.of())
                        .page(0)
                        .size(5)
                        .totalPages(1)
                        .totalElements(0)
                        .build();

        when(service.findAll(any(FlightPageRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/flights")
                        .param("departureCity", "MOSCOW")
                        .param("arrivalCity", "ST. PETERSBURG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.size()").value(0));

        ArgumentCaptor<FlightPageRequest> captor = ArgumentCaptor.forClass(
                FlightPageRequest.class
        );

        verify(service).findAll(captor.capture());

        FlightPageRequest request = captor.getValue();

        assertEquals(0, request.getPage());
        assertEquals(5, request.getSize());
        assertEquals("flightNumber", request.getSortBy());
        assertEquals("desc", request.getSortDirection());
        assertEquals("MOSCOW", request.getDepartureCity());
        assertEquals("ST. PETERSBURG", request.getArrivalCity());
    }

    @Test
    @DisplayName("GET /api/v1/flights/{flightNumber} - 200 Получить данные о полете по номеру")
    void shouldFindFlightByFlightNumber() throws Exception {
        FlightWithSeatsResponse response = FlightWithSeatsResponse.builder()
                .flightNumber("10A")
                .departureCity("MOSCOW")
                .arrivalCity("VLADIVOSTOK")
                .departureTime(LocalDateTime.now().plusDays(10))
                .arrivalTime(LocalDateTime.now().plusDays(10))
                .totalSeats(0L)
                .availableSeats(0L)
                .seats(List.of())
                .build();

        when(service.findByFlightNumber("10A", null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/flights/{flightNumber}", "10A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("10A"))
                .andExpect(jsonPath("$.departureCity").value("MOSCOW"))
                .andExpect(jsonPath("$.arrivalCity").value("VLADIVOSTOK"));

        verify(service).findByFlightNumber("10A", null);
    }

    @Test
    @DisplayName("GET /api/v1/flights/{flightNumber} - 404 Not found. Полет не найден по flightNumber")
    void shouldThrowFlightNotFoundException() throws Exception {
        when(service.findByFlightNumber("10A", null)).thenThrow(
                new FlightNotFoundException("10A")
        );

        mockMvc.perform(get("/api/v1/flights/{flightNumber}", "10A"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Полет не найден: номер полета = 10A"));

        verify(service).findByFlightNumber("10A", null);
    }
}