package ru.kalinin.payment.controller;

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
import ru.kalinin.common.exception.ExceptionAutoConfiguration;
import ru.kalinin.common.exception.payments.PaymentNotFoundException;
import ru.kalinin.common.exception.payments.PaymentUserNotEqualsException;
import ru.kalinin.common.test.config.WithMockJwtUser;
import ru.kalinin.payment.config.PaymentTestSecurityConfig;
import ru.kalinin.payment.dto.request.PaymentRequest;
import ru.kalinin.payment.dto.response.PaymentResponse;
import ru.kalinin.payment.entity.enums.PaymentStatus;
import ru.kalinin.payment.service.interfaces.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import({PaymentTestSecurityConfig.class, ExceptionAutoConfiguration.class})
@Tag("payment-controller")
public class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("Должен подтвердить оплату бронирования")
    @WithMockJwtUser(username = "kalinin", role = "USER")
    void shouldConfirmPayment() throws Exception {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .bookingNumber(UUID.randomUUID()).build();

        PaymentResponse response = PaymentResponse.builder()
                .paymentNumber(UUID.randomUUID())
                .bookingNumber(paymentRequest.getBookingNumber())
                .price(BigDecimal.valueOf(1250))
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.confirm("kalinin", paymentRequest.getBookingNumber())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentNumber").value(response.getPaymentNumber().toString()))
                .andExpect(jsonPath("$.bookingNumber").value(response.getBookingNumber().toString()))
                .andExpect(jsonPath("$.price").value(response.getPrice()))
                .andExpect(jsonPath("$.status").value(response.getStatus().toString()));

        verify(paymentService).confirm("kalinin", paymentRequest.getBookingNumber());
    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request если платеж принадлежит другому пользователю")
    @WithMockJwtUser(username = "kalinin", role = "USER")
    void shouldReturnBadRequestWhenPaymentBelongsToAnotherUser() throws Exception {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .bookingNumber(UUID.randomUUID()).build();

        when(paymentService.confirm("kalinin", paymentRequest.getBookingNumber())).thenThrow(
                new PaymentUserNotEqualsException("kalinin", paymentRequest.getBookingNumber())
        );

        mockMvc.perform(post("/api/v1/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка оплаты"))
                .andExpect(jsonPath("$.message").value(containsString("Бронь с номером: ")))
                .andExpect(jsonPath("$.message").value(containsString(paymentRequest.getBookingNumber().toString())))
                .andExpect(jsonPath("$.message").value(containsString("kalinin")));

        verify(paymentService).confirm("kalinin", paymentRequest.getBookingNumber());
    }

    @Test
    @DisplayName("Должен отменить оплату бронирования")
    @WithMockJwtUser(username = "kalinin", role = "USER")
    void shouldCancelPayment() throws Exception {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .bookingNumber(UUID.randomUUID()).build();

        PaymentResponse response = PaymentResponse.builder()
                .paymentNumber(UUID.randomUUID())
                .bookingNumber(paymentRequest.getBookingNumber())
                .price(BigDecimal.valueOf(1250))
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.cancel("kalinin", paymentRequest.getBookingNumber())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payment/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentNumber").value(response.getPaymentNumber().toString()))
                .andExpect(jsonPath("$.bookingNumber").value(response.getBookingNumber().toString()))
                .andExpect(jsonPath("$.price").value(response.getPrice()))
                .andExpect(jsonPath("$.status").value(response.getStatus().toString()));

        verify(paymentService).cancel("kalinin", paymentRequest.getBookingNumber());
    }


    @Test
    @DisplayName("Должен вернуть 404 Not Found если платеж не существует")
    @WithMockJwtUser(username = "kalinin", role = "USER")
    void shouldReturnNotFoundWhenPaymentDoesntExist() throws Exception {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .bookingNumber(UUID.randomUUID()).build();

        when(paymentService.cancel("kalinin", paymentRequest.getBookingNumber())).thenThrow(
                new PaymentNotFoundException(paymentRequest.getBookingNumber())
        );

        mockMvc.perform(post("/api/v1/payment/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value(containsString("Платеж не найден")))
                .andExpect(jsonPath("$.message").value(containsString(paymentRequest.getBookingNumber().toString())));

        verify(paymentService).cancel("kalinin", paymentRequest.getBookingNumber());
    }

    @Test
    @DisplayName("Должен вернуть историю платежей аутентифицированного пользователя")
    @WithMockJwtUser(username = "kalinin", role = "USER")
    void shouldReturnPaymentHistoryForAuthUser() throws Exception {
        List<PaymentResponse> responses = List.of(
                PaymentResponse.builder()
                        .paymentNumber(UUID.randomUUID())
                        .bookingNumber(UUID.randomUUID())
                        .price(BigDecimal.valueOf(1250))
                        .status(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .build(),
                PaymentResponse.builder()
                        .paymentNumber(UUID.randomUUID())
                        .bookingNumber(UUID.randomUUID())
                        .price(BigDecimal.valueOf(1500))
                        .status(PaymentStatus.FAILED)
                        .createdAt(LocalDateTime.now())
                        .build());
        when(paymentService.getUserHistory("kalinin")).thenReturn(responses);

        mockMvc.perform(get("/api/v1/payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentNumber")
                        .value(responses.get(0).getPaymentNumber().toString()))
                .andExpect(jsonPath("$[0].bookingNumber")
                        .value(responses.get(0).getBookingNumber().toString()))
                .andExpect(jsonPath("$[1].paymentNumber")
                        .value(responses.get(1).getPaymentNumber().toString()))
                .andExpect(jsonPath("$[1].bookingNumber")
                        .value(responses.get(1).getBookingNumber().toString()));

        verify(paymentService).getUserHistory("kalinin");
    }

    @Test
    @DisplayName("Должен вернуть 401 Unauthorized при попытке получить историю платежей без аутентификации")
    void shouldReturnUnauthorizedWhenGettingPaymentHistoryWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/payment"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("JWT token is missing or invalid"));

        verifyNoMoreInteractions(paymentService);
    }
}
