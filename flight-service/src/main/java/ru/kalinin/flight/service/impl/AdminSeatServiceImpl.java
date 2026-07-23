package ru.kalinin.flight.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.common.exception.seats.SeatNotFoundException;
import ru.kalinin.flight.dto.mapper.SeatMapper;
import ru.kalinin.flight.dto.request.SeatRequest;
import ru.kalinin.flight.dto.response.SeatAdminResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.Seat;
import ru.kalinin.flight.repository.SeatRepository;
import ru.kalinin.flight.service.FlightCacheService;
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
    private final FlightCacheService cacheService;

    @Override
    public List<SeatAdminResponse> findByFlightId(Long id) {
        Flight flight = adminFlightService.getById(id);

        List<Seat> seats = seatRepository.findSeatsByFlight(flight);

        return seatMapper.toSeatAdminResponse(seats);
    }

    @Override
    @CacheEvict(value = "flightPage", allEntries = true)
    public SeatAdminResponse create(SeatRequest request) {
        Flight flight = adminFlightService.getById(request.getFlightId());
        Seat seat = seatMapper.toEntity(request);
        seat.setFlight(flight);
        Seat savedSeat = seatRepository.save(seat);

        cacheService.evictFlight(seat.getFlight().getFlightNumber());

        return seatMapper.toSeatAdminResponse(savedSeat);
    }

    @Override
    @CacheEvict(value = "flightPage", allEntries = true)
    public SeatAdminResponse update(Long id, SeatRequest request) {
        Seat seat = getByIdWithFlight(id);
        String oldFlightNumber = seat.getFlight().getFlightNumber();
        Flight flight = adminFlightService.getById(request.getFlightId());

        seat.setFlight(flight);
        seat.setSeatNumber(request.getSeatNumber());
        seat.setStatus(request.getStatus());
        seat.setPrice(request.getPrice());

        Seat savedSeat = seatRepository.save(seat);

        cacheService.evictFlights(oldFlightNumber, flight.getFlightNumber());

        return seatMapper.toSeatAdminResponse(savedSeat);
    }

    @Override
    @CacheEvict(value = "flightPage", allEntries = true)
    public void delete(Long id) {
        Seat seat = getByIdWithFlight(id);

        seatRepository.delete(seat);

        cacheService.evictFlight(seat.getFlight().getFlightNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public Seat getById(Long id) {
        return seatRepository.findById(id).orElseThrow(
                ()-> new SeatNotFoundException(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Seat getByIdWithFlight(Long id) {
        return seatRepository.findByIdWithFlight(id).orElseThrow(
                ()-> new SeatNotFoundException(id)
        );
    }
}
