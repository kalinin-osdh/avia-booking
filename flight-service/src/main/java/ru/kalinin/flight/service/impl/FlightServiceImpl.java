package ru.kalinin.flight.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.common.dto.PageResponse;
import ru.kalinin.common.exception.flights.FlightNotFoundException;
import ru.kalinin.flight.dto.mapper.FlightMapper;
import ru.kalinin.flight.dto.mapper.SeatMapper;
import ru.kalinin.flight.dto.request.FlightPageRequest;
import ru.kalinin.flight.dto.response.FlightWithOutSeatsResponse;
import ru.kalinin.flight.dto.response.FlightWithSeatsResponse;
import ru.kalinin.flight.entity.Flight;
import ru.kalinin.flight.entity.enums.SeatStatus;
import ru.kalinin.flight.repository.FlightRepository;
import ru.kalinin.flight.repository.SeatRepository;
import ru.kalinin.flight.service.interfaces.FlightService;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {
    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    @Override
    @Cacheable(
            value = "flightPage",
            key = "T(String).format('%d:%d:%s:%s:%s:%s', " +
                    "#request.page, " +
                    "#request.size, " +
                    "#request.sortBy, " +
                    "#request.sortDirection, " +
                    "#request.departureCity == null ? '' : #request.departureCity, " +
                    "#request.arrivalCity == null ? '' : #request.arrivalCity)"
    )
    public PageResponse<FlightWithOutSeatsResponse> findAll(FlightPageRequest request) {
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

        return flightMapper.toPageResponse(page);
    }

    @Override
    @Cacheable(
            value = "flightByNumber",
            key = "T(String).format('%s:%s', " +
                    "#flightNumber, " +
                    "#status == null ? '' : #status.name())"
    )
    public FlightWithSeatsResponse findByFlightNumber(String flightNumber, SeatStatus status) {

        Flight flight = flightRepository.findByFlightNumberWithSeats(flightNumber, status).orElseThrow(
                ()-> new FlightNotFoundException(flightNumber)
        );

        return flightMapper.toFlightWithSeatsResponse(flight);
    }
}
