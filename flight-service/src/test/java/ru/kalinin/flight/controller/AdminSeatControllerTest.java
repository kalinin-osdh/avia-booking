package ru.kalinin.flight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.kalinin.common.test.config.WithMockJwtUser;
import ru.kalinin.common.test.config.TestSecurityConfig;
import ru.kalinin.flight.config.FlightTestSecurityConfig;
import ru.kalinin.flight.dto.request.SeatRequest;
import ru.kalinin.flight.dto.response.SeatAdminResponse;
import ru.kalinin.flight.entity.enums.SeatStatus;
import ru.kalinin.flight.service.interfaces.AdminSeatService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminSeatController.class)
@Import(FlightTestSecurityConfig.class)
@Tag("controller")
class AdminSeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private AdminSeatService service;

    private List<SeatAdminResponse> responses;

    @BeforeEach
    void setUpTestData() {
        responses = List.of(
                SeatAdminResponse.builder()
                        .id(1L)
                        .flightId(17L)
                        .seatNumber("1S")
                        .status(SeatStatus.AVAILABLE)
                        .price(BigDecimal.valueOf(1000))
                        .build(),
                SeatAdminResponse.builder()
                        .id(2L)
                        .flightId(17L)
                        .seatNumber("2S")
                        .status(SeatStatus.AVAILABLE)
                        .price(BigDecimal.valueOf(1250))
                        .build()
        );
    }

    @Test
    @DisplayName("GET /api/v1/admin/seats/flight/{id} - 200 Получить список мест полета по его ID")
    @WithMockJwtUser(role = "ADMIN")
    void shouldFindSeatsByFlightId() throws Exception {
        when(service.findByFlightId(17L)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/admin/seats/flight/{id}", 17))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].flightId").value(17L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].flightId").value(17L));

        verify(service).findByFlightId(17L);
    }

    @Test
    @DisplayName("POST /api/v1/admin/seats - 201 Создать место")
    @WithMockJwtUser(role = "ADMIN")
    void shouldCreateSeat() throws Exception {
        SeatRequest request = new SeatRequest(
                17L,
                "2S",
                SeatStatus.AVAILABLE,
                BigDecimal.valueOf(1250)
        );
        when(service.create(any(SeatRequest.class))).thenReturn(responses.get(1));

        mockMvc.perform(post("/api/v1/admin/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatNumber").value(responses.get(1).getSeatNumber()));

        verify(service, times(1)).create(any(SeatRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/seats/{id} - 200 Обновить место")
    @WithMockJwtUser(role = "ADMIN")
    void shouldUpdateSeat() throws Exception {
        SeatRequest request = new SeatRequest(
                17L,
                "2S",
                SeatStatus.AVAILABLE,
                BigDecimal.valueOf(2250)
        );

        SeatAdminResponse updatedResponse = SeatAdminResponse.builder()
                .id(2L)
                .flightId(17L)
                .seatNumber("2S")
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(2250))
                .build();

        when(service.update(eq(2L), any(SeatRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/admin/seats/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedResponse.getId()))
                .andExpect(jsonPath("$.price").value(updatedResponse.getPrice()));

        verify(service, times(1)).update(eq(2L), any(SeatRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/seats/{id} - 204 Удалить место")
    @WithMockJwtUser(role = "ADMIN")
    void shouldDeleteSeat() throws Exception {
        doNothing().when(service).delete(2L);

        mockMvc.perform(delete("/api/v1/admin/seats/{id}",2L))
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(2L);
    }
}