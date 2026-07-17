package ru.kalinin.flight.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.common.exception.flights.FlightExistsException;
import ru.kalinin.common.exception.flights.FlightNotFoundException;
import ru.kalinin.flight.dto.mapper.FlightMapper;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.request.FlightRequest;
import ru.kalinin.flight.dto.response.FlightAdminResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.repository.FlightRepository;
import ru.kalinin.flight.service.interfaces.AdminFlightService;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminFlightServiceImpl implements AdminFlightService {
    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    @Override
    public Page<FlightAdminResponse> findAll(FlightPageRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()),
                request.getSortBy());

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );

        Page<Flight> page = flightRepository.findAllWithFilter(
                request.getDepartureCity(),
                request.getArrivalCity(),
                pageable);

        return flightMapper.toAdminPageResponse(page);
    }

    @Override
    public FlightAdminResponse create(FlightRequest request) {
        if (flightRepository.existsFlightByFlightNumber(request.getFlightNumber()))
            throw new FlightExistsException(request.getFlightNumber());

        Flight flight = flightMapper.toEntity(request);

        Flight savedFlight = flightRepository.save(flight);

        return flightMapper.toFlightAdminResponse(savedFlight);
    }

    @Override
    public FlightAdminResponse update(Long id, FlightRequest request) {
        Flight flight = findById(id);

        flight.setDepartureCity(request.getDepartureCity());
        flight.setArrivalCity(request.getArrivalCity());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());

        Flight savedFlight = flightRepository.save(flight);

        return flightMapper.toFlightAdminResponse(savedFlight);
    }

    @Override
    public void delete(Long id) {
        Flight flight = findById(id);

        flightRepository.delete(flight);
    }

    @Override
    @Transactional(readOnly = true)
    public Flight findById(Long id) {
        return flightRepository.findById(id).orElseThrow(
                () -> new FlightNotFoundException(id)
        );
    }
}
