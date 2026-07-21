package ru.kalinin.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {
    @NotBlank(message = "Введите номер полета")
    private String flightNumber;
    @NotBlank(message = "Введите номер места")
    private String seatNumber;
}
