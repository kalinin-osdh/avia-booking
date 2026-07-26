package ru.kalinin.flight.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.flight.dto.response.PageResponse;
import ru.kalinin.common.exception.flights.FlightExistsException;
import ru.kalinin.common.exception.flights.FlightNotFoundException;
import ru.kalinin.flight.dto.mapper.FlightMapper;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.request.FlightRequest;
import ru.kalinin.flight.dto.request.FlightUpdateRequest;
import ru.kalinin.flight.dto.response.FlightAdminResponse;
import ru.kalinin.flight.dto.response.FlightAdminPageResponse;
import ru.kalinin.flight.dto.response.SeatCountsResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.repository.FlightRepository;
import ru.kalinin.flight.service.FlightCacheService;
import ru.kalinin.flight.service.interfaces.AdminFlightService;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminFlightServiceImpl implements AdminFlightService {
    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    private final FlightCacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FlightAdminPageResponse> findAll(FlightPageRequest request) {
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
    @CacheEvict(value = "flightPage", allEntries = true)
    public FlightAdminResponse create(FlightRequest request) {
        if (flightRepository.existsFlightByFlightNumber(request.getFlightNumber()))
            throw new FlightExistsException(request.getFlightNumber());

        Flight flight = flightMapper.toEntity(request);

        Flight savedFlight = flightRepository.save(flight);

        SeatCountsResponse counts = flightRepository.findCountSeats(savedFlight.getId());

        return flightMapper.toFlightAdminResponse(savedFlight, counts);
    }

    @Override
    @CacheEvict(value = "flightPage", allEntries = true)
    public FlightAdminResponse update(Long id, FlightUpdateRequest request) {
        Flight flight = getById(id);

        flight.setDepartureCity(request.getDepartureCity());
        flight.setArrivalCity(request.getArrivalCity());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());

        Flight savedFlight = flightRepository.save(flight);

        cacheService.evictFlight(savedFlight.getFlightNumber());

        SeatCountsResponse counts = flightRepository.findCountSeats(savedFlight.getId());


        return flightMapper.toFlightAdminResponse(savedFlight, counts);
    }

    @Override
    @CacheEvict(value = "flightPage", allEntries = true)
    public void delete(Long id) {
        Flight flight = getById(id);

        flightRepository.delete(flight);

        cacheService.evictFlight(flight.getFlightNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public FlightAdminResponse findById(Long id) {
        Flight flight = getById(id);
        SeatCountsResponse counts = flightRepository.findCountSeats(flight.getId());
        return flightMapper.toFlightAdminResponse(flight, counts);
    }

    @Override
    @Transactional(readOnly = true)
    public Flight getById(Long id) {
        return flightRepository.findById(id).orElseThrow(
                () -> new FlightNotFoundException(id)
        );
    }

}
