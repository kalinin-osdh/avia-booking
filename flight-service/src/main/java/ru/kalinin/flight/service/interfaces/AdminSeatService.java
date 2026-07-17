package ru.kalinin.flight.service.interfaces;

import ru.kalinin.flight.dto.request.SeatRequest;
import ru.kalinin.flight.dto.response.SeatAdminResponse;
import ru.kalinin.flight.entity.Seat;

import java.util.List;

public interface AdminSeatService {
    List<SeatAdminResponse> findByFlightId(Long id);
    SeatAdminResponse create(SeatRequest request);
    SeatAdminResponse update(Long id, SeatRequest request);
    void delete(Long id);
    Seat getById(Long id);
}
