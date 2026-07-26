package ru.kalinin.flight.service.interfaces;

import ru.kalinin.flight.dto.response.PageResponse;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.request.FlightRequest;
import ru.kalinin.flight.dto.request.FlightUpdateRequest;
import ru.kalinin.flight.dto.response.FlightAdminResponse;
import ru.kalinin.flight.dto.response.FlightAdminPageResponse;
import ru.kalinin.flight.entity.Flight;

public interface AdminFlightService {
    PageResponse<FlightAdminPageResponse> findAll(FlightPageRequest request);
    FlightAdminResponse create(FlightRequest request);
    FlightAdminResponse update(Long id, FlightUpdateRequest request);
    void delete(Long id);
    FlightAdminResponse findById(Long id);
    Flight getById(Long id);
}
