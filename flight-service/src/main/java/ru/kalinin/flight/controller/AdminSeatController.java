package ru.kalinin.flight.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.flight.dto.request.SeatRequest;
import ru.kalinin.flight.dto.response.SeatAdminResponse;
import ru.kalinin.flight.dto.response.SeatResponse;
import ru.kalinin.flight.service.interfaces.AdminSeatService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/seats")
@RequiredArgsConstructor
public class AdminSeatController {
    private final AdminSeatService adminSeatService;

    @GetMapping("/{id}")
    public ResponseEntity<List<SeatAdminResponse>> findSeatsByFlightId(@PathVariable Long id) {
        return ResponseEntity.ok(adminSeatService.findByFlightId(id));
    }

    @PostMapping
    public ResponseEntity<SeatAdminResponse> createSeat(@Valid @RequestBody SeatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminSeatService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeatAdminResponse> updateSeat(@PathVariable Long id, @Valid @RequestBody SeatRequest request) {
        return ResponseEntity.ok(adminSeatService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        adminSeatService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
