package ru.kalinin.booking.factory;

import ru.kalinin.booking.dto.request.BookingRequest;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.entity.Booking;
import ru.kalinin.booking.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class TestDataFactory {
    private TestDataFactory() {

    }

    public static BookingRequest createBookingRequest(String flightNumber,
                                                      String seatNumber) {
        return BookingRequest.builder()
                .flightNumber(flightNumber)
                .seatNumber(seatNumber)
                .build();
    }

    public static Booking createBooking(Long id,
                                        String flightNumber,
                                        String seatNumber) {
        return Booking.builder()
                .id(id)
                .username("kalinin")
                .flightNumber(flightNumber)
                .seatNumber(seatNumber)
                .build();
    }

    public static Booking createBooking(Long id,
                                        BookingRequest request) {
        return Booking.builder()
                .id(id)
                .username("kalinin")
                .flightNumber(request.getFlightNumber())
                .seatNumber(request.getSeatNumber())
                .build();
    }

    public static BookingResponse createBookingResponse(
            String flightNumber,
            String seatNumber,
            BigDecimal price,
            BookingStatus status) {
        return BookingResponse.builder()
                .bookingNumber(UUID.randomUUID())
                .flightNumber(flightNumber)
                .seatNumber(seatNumber)
                .price(price)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static BookingResponse createBookingResponse(BookingRequest request) {
        return BookingResponse.builder()
                .bookingNumber(UUID.randomUUID())
                .flightNumber(request.getFlightNumber())
                .seatNumber(request.getSeatNumber())
                .price(BigDecimal.valueOf(1000))
                .status(BookingStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static BookingResponse createBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingNumber(booking.getBookingNumber())
                .flightNumber(booking.getFlightNumber())
                .seatNumber(booking.getSeatNumber())
                .price(booking.getPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
