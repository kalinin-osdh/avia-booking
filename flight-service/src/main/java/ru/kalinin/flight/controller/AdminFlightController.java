package ru.kalinin.flight.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.request.FlightRequest;
import ru.kalinin.flight.dto.response.FlightAdminResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.service.interfaces.AdminFlightService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/flights")
@RequiredArgsConstructor
public class AdminFlightController {
    private final AdminFlightService adminFlightService;

    @GetMapping
    public ResponseEntity<Page<FlightAdminResponse>> findAllFlights(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String departureCity,
            @RequestParam(required = false) String arrivalCity){
        FlightPageRequest request = FlightPageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .departureCity(departureCity)
                .arrivalCity(arrivalCity)
                .build();
        return ResponseEntity.ok().body(adminFlightService.findAll(request));
    }

    @PostMapping
    public ResponseEntity<FlightAdminResponse> createFlight(@Valid @RequestBody FlightRequest request){
        // todo создание полета
        return null;
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightAdminResponse> updateFlight(@PathVariable Long id, @Valid @RequestBody FlightRequest request){
        // todo обновление полета
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id){
        // todo удаление полета
        return null;
    }
}
