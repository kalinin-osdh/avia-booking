package ru.kalinin.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.booking.dto.request.BookingRequest;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.service.interfaces.BookingService;
import ru.kalinin.common.security.dto.JwtUserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> booking(
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.booking(userPrincipal.username(), request));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> bookingHistory(@AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        return ResponseEntity.ok(bookingService.getUserHistory(userPrincipal.username()));
    }

}
