package ru.kalinin.flight.service.interfaces;

import org.springframework.data.domain.Page;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.request.FlightRequest;
import ru.kalinin.flight.dto.response.FlightAdminResponse;
import ru.kalinin.flight.dto.response.FlightWithoutSeatsResponse;
import ru.kalinin.flight.entity.Flight;

import java.util.List;

public interface AdminFlightService {
    Page<FlightAdminResponse> findAll(FlightPageRequest request);
    FlightAdminResponse create(FlightRequest request);
    FlightAdminResponse update(Long id, FlightRequest request);
    void delete(Long id);
    Flight findById(Long id);
}
