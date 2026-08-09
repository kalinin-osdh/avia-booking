package ru.kalinin.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.kalinin.booking.config.BookingTestSecurityConfig;
import ru.kalinin.booking.dto.request.BookingRequest;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.entity.enums.BookingStatus;
import ru.kalinin.booking.factory.TestDataFactory;
import ru.kalinin.booking.service.interfaces.BookingService;
import ru.kalinin.common.exception.ExceptionAutoConfiguration;
import ru.kalinin.common.test.config.WithMockJwtUser;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(BookingController.class)
@Import({BookingTestSecurityConfig.class, ExceptionAutoConfiguration.class})
@Tag("booking-controller")
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService service;

    @Test
    @DisplayName("Должен создать бронь для аутентифицированного пользователя")
    @WithMockJwtUser(username = "kalinin", role = "USER")
    void shouldBookFlightSeat() throws Exception {
        BookingRequest request = TestDataFactory.createBookingRequest("1A", "1S");

        BookingResponse response = TestDataFactory.createBookingResponse(request);

        when(service.booking(eq("kalinin"), any(BookingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingNumber").value(response.getBookingNumber().toString()))
                .andExpect(jsonPath("$.flightNumber").value(response.getFlightNumber()))
                .andExpect(jsonPath("$.seatNumber").value(response.getSeatNumber()))
                .andExpect(jsonPath("$.status").value(response.getStatus().toString()))
                .andExpect(jsonPath("$.price").value(response.getPrice()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        verify(service).booking(
                eq("kalinin"),
                argThat(r ->
                        r.getFlightNumber().equals(request.getFlightNumber()) &&
                                r.getSeatNumber().equals(request.getSeatNumber())
                ));
    }

    @Test
    @DisplayName("Должен вернуть 401 Unauthorized при попытке забронировать место без аутентификации")
    void shouldReturnUnauthorizedWhenBookFlightSeat() throws Exception {
        BookingRequest request = TestDataFactory.createBookingRequest("1A", "1S");

        mockMvc.perform(post("/api/v1/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("JWT token is missing or invalid"));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Должен вернуть 400 BadRequest при ошибке валидации данных")
    @WithMockJwtUser(role = "USER")
    void shouldReturnBadRequestWhenBookFlightSeat() throws Exception {
        BookingRequest request = TestDataFactory.createBookingRequest("", "1S");

        mockMvc.perform(post("/api/v1/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.message").value("flightNumber: Введите номер полета"));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Должен вернуть историю броней аутентифицированного пользователя")
    @WithMockJwtUser(role = "USER")
    void shouldReturnBookingHistoryForAuthUser() throws Exception {
        List<BookingResponse> responses = List.of(
                TestDataFactory.createBookingResponse("1A", "1S", BigDecimal.valueOf(1000), BookingStatus.CREATED),
                TestDataFactory.createBookingResponse("1B", "1S", BigDecimal.valueOf(1000), BookingStatus.CREATED)
        );


        when(service.getUserHistory("kalinin")).thenReturn(responses);

        mockMvc.perform(get("/api/v1/booking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingNumber").value(responses.get(0).getBookingNumber().toString()))
                .andExpect(jsonPath("$[1].bookingNumber").value(responses.get(1).getBookingNumber().toString()));

        verify(service).getUserHistory("kalinin");
    }

    @Test
    @DisplayName("Должен вернуть 401 Unauthorized при попытке получить историю без аутентификации")
    void shouldReturnUnauthorizedWhenGetBookingHistory() throws Exception {
        mockMvc.perform(get("/api/v1/booking"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("JWT token is missing or invalid"));

        verifyNoInteractions(service);
    }
}
