package ru.kalinin.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.common.security.dto.JwtUserPrincipal;
import ru.kalinin.payment.dto.request.PaymentRequest;
import ru.kalinin.payment.dto.response.PaymentResponse;
import ru.kalinin.payment.service.interfaces.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<PaymentResponse> confirm(
            @AuthenticationPrincipal JwtUserPrincipal user,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.confirm(user.username(), request.getBookingNumber()));
    }

    @PostMapping("/cancel")
    public ResponseEntity<PaymentResponse> cancel(
            @AuthenticationPrincipal JwtUserPrincipal user,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.cancel(user.username(), request.getBookingNumber()));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> paymentHistory(@AuthenticationPrincipal JwtUserPrincipal user) {
        return ResponseEntity.ok(paymentService.getUserHistory(user.username()));
    }
}
