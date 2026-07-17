package ru.kalinin.flight.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.common.exception.seats.SeatNotFoundException;
import ru.kalinin.flight.dto.mapper.SeatMapper;
import ru.kalinin.flight.dto.request.SeatRequest;
import ru.kalinin.flight.dto.response.FlightAdminResponse;
import ru.kalinin.flight.dto.response.SeatAdminResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.Seat;
import ru.kalinin.flight.repository.SeatRepository;
import ru.kalinin.flight.service.interfaces.AdminFlightService;
import ru.kalinin.flight.service.interfaces.AdminSeatService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminSeatServiceImpl implements AdminSeatService {
    private final SeatRepository seatRepository;
    private final AdminFlightService adminFlightService;
    private final SeatMapper seatMapper;

    @Override
    public List<SeatAdminResponse> findByFlightId(Long id) {
        Flight flight = adminFlightService.findById(id);

        List<Seat> seats = seatRepository.findSeatsByFlight(flight);

        return seatMapper.toSeatAdminResponse(seats);
    }

    @Override
    public SeatAdminResponse create(SeatRequest request) {
        Flight flight = adminFlightService.findById(request.getFlightId());
        Seat seat = seatMapper.toEntity(request);
        seat.setFlight(flight);

        Seat savedSeat = seatRepository.save(seat);

        return seatMapper.toSeatAdminResponse(savedSeat);
    }

    @Override
    public SeatAdminResponse update(Long id, SeatRequest request) {
        Seat seat = findById(id);
        Flight flight = adminFlightService.findById(request.getFlightId());

        seat.setFlight(flight);
        seat.setSeatNumber(request.getSeatNumber());
        seat.setStatus(request.getStatus());
        seat.setPrice(request.getPrice());

        Seat savedSeat = seatRepository.save(seat);

        return seatMapper.toSeatAdminResponse(savedSeat);
    }

    @Override
    public void delete(Long id) {
        Seat seat = findById(id);

        seatRepository.delete(seat);
    }

    @Override
    public Seat findById(Long id) {
        return seatRepository.findById(id).orElseThrow(
                ()-> new SeatNotFoundException(id)
        );
    }
}
