package ru.kalinin.flight.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.flight.dto.request.SeatRequest;
import ru.kalinin.flight.dto.response.SeatAdminResponse;
import ru.kalinin.flight.dto.response.SeatResponse;

@RestController
@RequestMapping("/api/v1/admin/seats")
public class AdminSeatController {

    @GetMapping("/{id}")
    public ResponseEntity<SeatAdminResponse> findSeatsByFlightId(@PathVariable Long id){
        // todo получить список мест определенного полета
        return null;
    }

    @PostMapping
    public ResponseEntity<SeatAdminResponse> createSeat(@Valid @RequestBody SeatRequest request){
        // todo создание полета
        return null;
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeatAdminResponse> updateSeat(@PathVariable Long id, @Valid @RequestBody SeatRequest request){
        // todo обновление полета
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id){
        // todo удаление полета
        return null;
    }

}
