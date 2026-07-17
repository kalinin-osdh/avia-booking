package ru.kalinin.flight.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Value;
import ru.kalinin.flight.entity.enums.SeatStatus;

import java.math.BigDecimal;

@Data
@Builder
public class SeatResponse {
    private String seatNumber;
    private SeatStatus status;
    private BigDecimal price;
}
