package ru.kalinin.flight.dto;

import ru.kalinin.flight.dto.response.SeatCountsResponse;

public class SeatCountsResponseTest implements SeatCountsResponse {
    private final Long totalSeats;
    private final Long availableSeats;

    public SeatCountsResponseTest(Long totalSeats, Long availableSeats) {
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
    }


    @Override
    public Long getTotalSeats() {
        return totalSeats;
    }

    @Override
    public Long getAvailableSeats() {
        return availableSeats;
    }
}
