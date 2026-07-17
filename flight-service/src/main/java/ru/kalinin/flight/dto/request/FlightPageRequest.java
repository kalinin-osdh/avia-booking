package ru.kalinin.flight.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightPageRequest {
    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 5;

    @Builder.Default
    private String sortBy = "flightNumber";

    @Builder.Default
    private String sortDirection = "desc";

    private String departureCity;
    private String arrivalCity;
}
