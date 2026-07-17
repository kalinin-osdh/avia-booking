package ru.kalinin.flight.service.interfaces;

import org.springframework.data.domain.Page;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.response.FlightWithOutSeatsResponse;
import ru.kalinin.flight.dto.response.FlightWithSeatsResponse;
import ru.kalinin.flight.entity.enums.SeatStatus;

public interface FlightService {
    Page<FlightWithOutSeatsResponse> findAll(FlightPageRequest request);
    FlightWithSeatsResponse findByFlightNumber(String flightNumber, SeatStatus status);
}
