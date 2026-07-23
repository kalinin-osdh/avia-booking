package ru.kalinin.flight.service.interfaces;

import ru.kalinin.common.dto.PageResponse;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.response.FlightWithOutSeatsResponse;
import ru.kalinin.flight.dto.response.FlightWithSeatsResponse;
import ru.kalinin.flight.entity.enums.SeatStatus;

import java.math.BigDecimal;

public interface FlightService {
    PageResponse<FlightWithOutSeatsResponse> findAll(FlightPageRequest request);
    FlightWithSeatsResponse findByFlightNumber(String flightNumber, SeatStatus status);
    BigDecimal reserveSeat(String flightNumber, String seatNumber);
}
