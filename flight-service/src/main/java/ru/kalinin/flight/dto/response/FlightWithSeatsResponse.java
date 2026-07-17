package ru.kalinin.flight.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FlightWithSeatsResponse {
    private String flightNumber;
    private String departureCity;
    private String arrivalCity;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime departureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arrivalTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private List<SeatResponse> seats;
}
