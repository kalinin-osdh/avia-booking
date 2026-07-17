package ru.kalinin.flight.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.common.exception.seats.SeatNotFoundException;
import ru.kalinin.flight.dto.mapper.SeatMapper;
import ru.kalinin.flight.dto.request.SeatRequest;
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
        Flight flight = adminFlightService.getById(id);

        List<Seat> seats = seatRepository.findSeatsByFlight(flight);

        return seatMapper.toSeatAdminResponse(seats);
    }

    @Override
    public SeatAdminResponse create(SeatRequest request) {
        Flight flight = adminFlightService.getById(request.getFlightId());
        Seat seat = seatMapper.toEntity(request);
        seat.setFlight(flight);
        int result = adminFlightService.changeAvailableSeats(flight.getId(), 1);
        if (result==0)
            throw new IllegalStateException("someException");
        Seat savedSeat = seatRepository.save(seat);

        return seatMapper.toSeatAdminResponse(savedSeat);
    }

    @Override
    public SeatAdminResponse update(Long id, SeatRequest request) {
        Seat seat = getById(id);
        Flight flight = adminFlightService.getById(request.getFlightId());

        seat.setFlight(flight);
        seat.setSeatNumber(request.getSeatNumber());
        seat.setStatus(request.getStatus());
        seat.setPrice(request.getPrice());

        Seat savedSeat = seatRepository.save(seat);

        return seatMapper.toSeatAdminResponse(savedSeat);
    }

    @Override
    public void delete(Long id) {
        Seat seat = getById(id);
        int result = adminFlightService.changeAvailableSeats(seat.getFlight().getId(), -1);
        if (result==0)
            throw new IllegalStateException("someException");
        seatRepository.delete(seat);
    }

    @Override
    @Transactional(readOnly = true)
    public Seat getById(Long id) {
        return seatRepository.findById(id).orElseThrow(
                ()-> new SeatNotFoundException(id)
        );
    }
}
