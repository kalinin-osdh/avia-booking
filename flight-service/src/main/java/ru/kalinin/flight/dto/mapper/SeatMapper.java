package ru.kalinin.flight.dto.mapper;

import org.springframework.stereotype.Component;
import ru.kalinin.flight.dto.request.SeatRequest;
import ru.kalinin.flight.dto.response.SeatAdminResponse;
import ru.kalinin.flight.dto.response.SeatResponse;
import ru.kalinin.flight.entity.Seat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SeatMapper {
    public Seat toEntity(SeatRequest request) {
        return Seat.builder()
                .seatNumber(request.getSeatNumber())
                .status(request.getStatus())
                .price(request.getPrice())
                .build();
    }

    public SeatAdminResponse toSeatAdminResponse(Seat seat) {
        return SeatAdminResponse.builder()
                .id(seat.getId())
                .flightId(seat.getFlight().getId())
                .seatNumber(seat.getSeatNumber())
                .status(seat.getStatus())
                .price(seat.getPrice())
                .build();
    }

    public SeatResponse toSeatResponse(Seat seat) {
        return SeatResponse.builder()
                .seatNumber(seat.getSeatNumber())
                .status(seat.getStatus())
                .price(seat.getPrice())
                .build();
    }

    public List<SeatAdminResponse> toSeatAdminResponse(List<Seat> seats) {
        if (seats.isEmpty())
            return new ArrayList<>();
        return seats.stream().map(this::toSeatAdminResponse).collect(Collectors.toList());
    }

    public List<SeatResponse> toSeatResponse(List<Seat> seats) {
        if(seats.isEmpty()){
            return new ArrayList<>();
        }
        return seats.stream().map(this::toSeatResponse).collect(Collectors.toList());

    }
}
