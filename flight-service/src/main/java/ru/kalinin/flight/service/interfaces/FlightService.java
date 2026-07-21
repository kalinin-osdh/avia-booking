package ru.kalinin.flight.service.interfaces;

import ru.kalinin.common.dto.PageResponse;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.response.FlightWithOutSeatsResponse;
import ru.kalinin.flight.dto.response.FlightWithSeatsResponse;
import ru.kalinin.flight.entity.enums.SeatStatus;

public interface FlightService {
    PageResponse<FlightWithOutSeatsResponse> findAll(FlightPageRequest request);
    FlightWithSeatsResponse findByFlightNumber(String flightNumber, SeatStatus status);
    void reserveSeat(String flightNumber, String seatNumber);
}
