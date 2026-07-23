package ru.kalinin.flight.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.kalinin.common.dto.PageResponse;
import ru.kalinin.flight.dto.request.FlightRequest;
import ru.kalinin.flight.dto.response.*;
import ru.kalinin.flight.entity.Flight;

@Component
@RequiredArgsConstructor
public class FlightMapper {
    private final SeatMapper seatMapper;

    public Flight toEntity(FlightRequest request) {
        return Flight.builder()
                .flightNumber(request.getFlightNumber())
                .departureCity(request.getDepartureCity())
                .arrivalCity(request.getArrivalCity())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .build();
    }

    public PageResponse<FlightAdminPageResponse> toAdminPageResponse(Page<Flight> page) {
        Page<FlightAdminPageResponse> mappedPage = page.map(this::toAdminPageResponse);
        PageResponse<FlightAdminPageResponse> responsePage = new PageResponse<>();
        responsePage.setContent(mappedPage.getContent());
        responsePage.setPage(mappedPage.getNumber());
        responsePage.setSize(mappedPage.getSize());
        responsePage.setTotalPages(mappedPage.getTotalPages());
        responsePage.setTotalElements(mappedPage.getTotalElements());
        return responsePage;
    }

    public PageResponse<FlightWithOutSeatsResponse> toPageResponse(Page<Flight> page) {
        Page<FlightWithOutSeatsResponse> mappedPage = page.map(this::toUsersPageResponse);
        PageResponse<FlightWithOutSeatsResponse> responsePage = new PageResponse<>();
        responsePage.setContent(mappedPage.getContent());
        responsePage.setPage(mappedPage.getNumber());
        responsePage.setSize(mappedPage.getSize());
        responsePage.setTotalPages(mappedPage.getTotalPages());
        responsePage.setTotalElements(mappedPage.getTotalElements());
        return responsePage;
    }

    public FlightAdminResponse toFlightAdminResponse(Flight flight, SeatCountsResponse counts) {
        return FlightAdminResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .departureCity(flight.getDepartureCity())
                .arrivalCity(flight.getArrivalCity())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(counts.getTotalSeats())
                .availableSeats(counts.getAvailableSeats())
                .seats(seatMapper.toSeatResponse(flight.getSeats()))
                .build();
    }

    public FlightWithSeatsResponse toFlightWithSeatsResponse(Flight flight, SeatCountsResponse counts) {
        return FlightWithSeatsResponse.builder()
                .flightNumber(flight.getFlightNumber())
                .departureCity(flight.getDepartureCity())
                .arrivalCity(flight.getArrivalCity())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(counts.getTotalSeats())
                .availableSeats(counts.getAvailableSeats())
                .seats(seatMapper.toSeatResponse(flight.getSeats()))
                .build();
    }

    public FlightAdminPageResponse toAdminPageResponse(Flight flight){
        return FlightAdminPageResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .departureCity(flight.getDepartureCity())
                .arrivalCity(flight.getArrivalCity())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .seats(seatMapper.toSeatResponse(flight.getSeats()))
                .build();
    }

    public FlightWithOutSeatsResponse toUsersPageResponse(Flight flight) {
        return FlightWithOutSeatsResponse.builder()
                .flightNumber(flight.getFlightNumber())
                .departureCity(flight.getDepartureCity())
                .arrivalCity(flight.getArrivalCity())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .build();
    }
}
