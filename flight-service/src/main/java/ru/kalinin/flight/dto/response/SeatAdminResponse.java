package ru.kalinin.flight.dto.response;

import lombok.Builder;
import lombok.Data;
import ru.kalinin.flight.entity.enums.SeatStatus;

import java.math.BigDecimal;

@Data
@Builder
public class SeatAdminResponse {
    private Long id;
    private Long flightId;
    private String seatNumber;
    private SeatStatus status;
    private BigDecimal price;
}
