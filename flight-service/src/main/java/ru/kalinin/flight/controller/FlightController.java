package ru.kalinin.flight.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.flight.dto.response.PageResponse;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.response.FlightWithOutSeatsResponse;
import ru.kalinin.flight.dto.response.FlightWithSeatsResponse;
import ru.kalinin.flight.entity.enums.SeatStatus;
import ru.kalinin.flight.service.interfaces.FlightService;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {
    private final FlightService flightService;

    @GetMapping
    public ResponseEntity<PageResponse<FlightWithOutSeatsResponse>> findFlights(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(defaultValue = "flightNumber") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String departureCity,
            @RequestParam(required = false) String arrivalCity) {
        FlightPageRequest request = FlightPageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .departureCity(departureCity)
                .arrivalCity(arrivalCity)
                .build();
        return ResponseEntity.ok().body(flightService.findAll(request));
    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<FlightWithSeatsResponse> findFlightByFlightNumber(
            @PathVariable String flightNumber,
            @RequestParam(required = false) SeatStatus status) {

        return ResponseEntity.ok().body(flightService.findByFlightNumber(flightNumber, status));
    }
}
