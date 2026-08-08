
package ru.kalinin.flight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.kalinin.common.exception.GlobalExceptionHandler;
import ru.kalinin.common.exception.flights.FlightNotFoundException;
import ru.kalinin.common.test.config.WithMockJwtUser;
import ru.kalinin.common.test.config.TestSecurityConfig;
import ru.kalinin.flight.config.FlightTestSecurityConfig;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.request.FlightRequest;
import ru.kalinin.flight.dto.request.FlightUpdateRequest;
import ru.kalinin.flight.dto.response.FlightAdminPageResponse;
import ru.kalinin.flight.dto.response.FlightAdminResponse;
import ru.kalinin.flight.dto.response.PageResponse;
import ru.kalinin.flight.service.interfaces.AdminFlightService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AdminFlightController.class)
@Import({FlightTestSecurityConfig.class, GlobalExceptionHandler.class})
@Tag("controller")
class AdminFlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private AdminFlightService service;

    @Test
    @DisplayName("GET /api/v1/admin/flights - 200 Получить список всех полетов с фильтрами (пагинация)")
    @WithMockJwtUser(role = "ADMIN")
    void shouldFindAllFlights() throws Exception {
        PageResponse<FlightAdminPageResponse> pageResponse =
                PageResponse.<FlightAdminPageResponse>builder()
                        .content(List.of())
                        .page(0)
                        .size(25)
                        .totalPages(1)
                        .totalElements(0)
                        .build();

        when(service.findAll(any(FlightPageRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/admin/flights")
                        .param("page", "0")
                        .param("size", "25")
                        .param("sortBy", "id")
                        .param("sortDirection", "asc")
                        .param("departureCity", "MOSCOW")
                        .param("arrivalCity", "ST. PETERSBURG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(25))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.size()").value(0));

        ArgumentCaptor<FlightPageRequest> captor = ArgumentCaptor.forClass(
                FlightPageRequest.class
        );

        verify(service).findAll(captor.capture());

        FlightPageRequest request = captor.getValue();

        assertEquals(0, request.getPage());
        assertEquals(25, request.getSize());
        assertEquals("id", request.getSortBy());
        assertEquals("asc", request.getSortDirection());
        assertEquals("MOSCOW", request.getDepartureCity());
        assertEquals("ST. PETERSBURG", request.getArrivalCity());
    }

    @Test
    @DisplayName("GET /api/v1/admin/flights - 200 Получить список всех полетов (пагинация)")
    @WithMockJwtUser(role = "ADMIN")
    void shouldFindAllFlightsWithDefaultPageSettings() throws Exception {
        PageResponse<FlightAdminPageResponse> pageResponse =
                PageResponse.<FlightAdminPageResponse>builder()
                        .content(List.of())
                        .page(0)
                        .size(5)
                        .totalPages(1)
                        .totalElements(0)
                        .build();

        when(service.findAll(any(FlightPageRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/admin/flights"))
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
        assertEquals("id", request.getSortBy());
        assertEquals("desc", request.getSortDirection());
        assertNull(request.getDepartureCity());
        assertNull(request.getArrivalCity());
    }

    @Test
    @DisplayName("GET /api/v1/admin/flights/{id} - 200 Получить полет по ID")
    @WithMockJwtUser(role = "ADMIN")
    void shouldFindFlightById() throws Exception {
        FlightAdminResponse response = FlightAdminResponse.builder()
                .id(10L)
                .flightNumber("10A")
                .departureCity("MOSCOW")
                .arrivalCity("VLADIVOSTOK")
                .departureTime(LocalDateTime.now().plusDays(10))
                .arrivalTime(LocalDateTime.now().plusDays(10))
                .totalSeats(0L)
                .availableSeats(0L)
                .seats(List.of())
                .build();

        when(service.findById(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/flights/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.flightNumber").value("10A"))
                .andExpect(jsonPath("$.departureCity").value("MOSCOW"))
                .andExpect(jsonPath("$.arrivalCity").value("VLADIVOSTOK"))
                .andExpect(jsonPath("$.seats.size()").value(0));

        verify(service).findById(10L);
    }

    @Test
    @DisplayName("GET /api/v1/admin/flights/{id} - 404 Not found. Полет не найден по ID")
    @WithMockJwtUser(role = "ADMIN")
    void shouldThrowFlightNotFoundException() throws Exception {
        when(service.findById(100L)).thenThrow(
                new FlightNotFoundException(100L)
        );

        mockMvc.perform(get("/api/v1/admin/flights/{id}", 100L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Полет не найден: id = 100"));

        verify(service).findById(100L);
    }

    @Test
    @DisplayName("POST /api/v1/admin/flights - 201 Создать полет")
    @WithMockJwtUser(role = "ADMIN")
    void shouldCreateFlight() throws Exception{
        FlightRequest request = FlightRequest.builder()
                .flightNumber("10A")
                .departureCity("MOSCOW")
                .arrivalCity("VLADIVOSTOK")
                .departureTime(LocalDateTime.now().plusDays(10))
                .arrivalTime(LocalDateTime.now().plusDays(10))
                .build();

        FlightAdminResponse response = FlightAdminResponse.builder()
                .id(10L)
                .flightNumber("10A")
                .departureCity("MOSCOW")
                .arrivalCity("VLADIVOSTOK")
                .departureTime(LocalDateTime.now().plusDays(10))
                .arrivalTime(LocalDateTime.now().plusDays(10))
                .totalSeats(0L)
                .availableSeats(0L)
                .seats(List.of())
                .build();

        when(service.create(any(FlightRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("10A"))
                .andExpect(jsonPath("$.departureCity").value("MOSCOW"))
                .andExpect(jsonPath("$.arrivalCity").value("VLADIVOSTOK"));

        verify(service, times(1)).create(any(FlightRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/flights/{id} - 200 Обновить данные о полете")
    @WithMockJwtUser(role = "ADMIN")
    void shouldUpdateFlight() throws Exception{
        FlightUpdateRequest request = FlightUpdateRequest.builder()
                .departureCity("MOSCOW")
                .arrivalCity("ST. PETERSBURG")
                .departureTime(LocalDateTime.now().plusDays(10))
                .arrivalTime(LocalDateTime.now().plusDays(10))
                .build();

        FlightAdminResponse response = FlightAdminResponse.builder()
                .id(10L)
                .flightNumber("10A")
                .departureCity("MOSCOW")
                .arrivalCity("ST. PETERSBURG")
                .departureTime(LocalDateTime.now().plusDays(10))
                .arrivalTime(LocalDateTime.now().plusDays(10))
                .totalSeats(0L)
                .availableSeats(0L)
                .seats(List.of())
                .build();

        when(service.update(eq(10L), any(FlightUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/flights/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("10A"))
                .andExpect(jsonPath("$.departureCity").value("MOSCOW"))
                .andExpect(jsonPath("$.arrivalCity").value("ST. PETERSBURG"));

        verify(service, times(1)).update(eq(10L), any(FlightUpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/flights/{id} - 204 Удаление полета")
    @WithMockJwtUser(role = "ADMIN")
    void shouldDeleteFlight() throws Exception {
        doNothing().when(service).delete(10L);

        mockMvc.perform(delete("/api/v1/admin/flights/{id}", 10L))
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(10L);
    }
}