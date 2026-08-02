package ru.kalinin.flight.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.flight.dto.response.PageResponse;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.request.FlightRequest;
import ru.kalinin.flight.dto.request.FlightUpdateRequest;
import ru.kalinin.flight.dto.response.FlightAdminResponse;
import ru.kalinin.flight.dto.response.FlightAdminPageResponse;
import ru.kalinin.flight.service.interfaces.AdminFlightService;

@RestController
@RequestMapping("/api/v1/admin/flights")
@RequiredArgsConstructor
public class AdminFlightController {
    private final AdminFlightService adminFlightService;

    @GetMapping
    public ResponseEntity<PageResponse<FlightAdminPageResponse>> findAllFlights(
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

    @GetMapping("/{id}")
    public ResponseEntity<FlightAdminResponse> findFlightById(@PathVariable Long id){
        return ResponseEntity.ok(adminFlightService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FlightAdminResponse> createFlight(@Valid @RequestBody FlightRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(adminFlightService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightAdminResponse> updateFlight(@PathVariable Long id, @Valid @RequestBody FlightUpdateRequest request){
        return ResponseEntity.ok(adminFlightService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id){
        adminFlightService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
