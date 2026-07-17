package ru.kalinin.flight.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.kalinin.common.dto.PageResponse;
import ru.kalinin.flight.dto.request.FlightRequest;
import ru.kalinin.flight.dto.response.FlightAdminResponse;
import ru.kalinin.flight.dto.response.FlightWithOutSeatsResponse;
import ru.kalinin.flight.dto.response.FlightWithSeatsResponse;
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
                .totalSeats(request.getTotalSeats())
                .availableSeats(0)
                .build();
    }

    public PageResponse<FlightAdminResponse> toAdminPageResponse(Page<Flight> page) {
        Page<FlightAdminResponse> mappedPage = page.map(this::toFlightAdminResponse);
        PageResponse<FlightAdminResponse> responsePage = new PageResponse<>();
        responsePage.setContent(mappedPage.getContent());
        responsePage.setPage(mappedPage.getNumber());
        responsePage.setSize(mappedPage.getSize());
        responsePage.setTotalPages(mappedPage.getTotalPages());
        responsePage.setTotalElements(mappedPage.getTotalElements());
        return responsePage;
    }

    public PageResponse<FlightWithOutSeatsResponse> toPageResponse(Page<Flight> page) {
        Page<FlightWithOutSeatsResponse> mappedPage = page.map(this::toFlightWithOutSeatsResponse);
        PageResponse<FlightWithOutSeatsResponse> responsePage = new PageResponse<>();
        responsePage.setContent(mappedPage.getContent());
        responsePage.setPage(mappedPage.getNumber());
        responsePage.setSize(mappedPage.getSize());
        responsePage.setTotalPages(mappedPage.getTotalPages());
        responsePage.setTotalElements(mappedPage.getTotalElements());
        return responsePage;
    }

    public FlightAdminResponse toFlightAdminResponse(Flight flight) {
        return FlightAdminResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .departureCity(flight.getDepartureCity())
                .arrivalCity(flight.getArrivalCity())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .seats(seatMapper.toSeatResponse(flight.getSeats()))
                .build();
    }

    public FlightWithOutSeatsResponse toFlightWithOutSeatsResponse(Flight flight){
        return FlightWithOutSeatsResponse.builder()
                .flightNumber(flight.getFlightNumber())
                .departureCity(flight.getDepartureCity())
                .arrivalCity(flight.getArrivalCity())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .build();
    }

    public FlightWithSeatsResponse toFlightWithSeatsResponse(Flight flight) {
        return FlightWithSeatsResponse.builder()
                .flightNumber(flight.getFlightNumber())
                .departureCity(flight.getDepartureCity())
                .arrivalCity(flight.getArrivalCity())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .seats(seatMapper.toSeatResponse(flight.getSeats()))
                .build();
    }
}
