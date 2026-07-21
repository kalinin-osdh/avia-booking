package ru.kalinin.booking.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.kalinin.booking.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private String bookingNumber;
    private String username;
    private String flightNumber;
    private String seatNumber;
    private BigDecimal price;
    private BookingStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
