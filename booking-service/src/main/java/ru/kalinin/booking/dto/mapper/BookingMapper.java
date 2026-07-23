package ru.kalinin.booking.dto.mapper;

import org.springframework.stereotype.Component;
import ru.kalinin.booking.dto.request.BookingRequest;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.entity.Booking;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    public Booking toEntity(BookingRequest request, String username){
        return Booking.builder()
                .username(username)
                .flightNumber(request.getFlightNumber())
                .seatNumber(request.getSeatNumber())
                .build();
    }

    public BookingResponse toResponse(Booking booking){
        return BookingResponse.builder()
                .bookingNumber(booking.getBookingNumber())
                .username(booking.getUsername())
                .flightNumber(booking.getFlightNumber())
                .seatNumber(booking.getSeatNumber())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .price(booking.getPrice())
                .build();
    }

    public List<BookingResponse> toResponse(List<Booking> booking){
        return booking.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
