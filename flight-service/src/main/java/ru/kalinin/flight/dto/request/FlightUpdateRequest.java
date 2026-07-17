package ru.kalinin.flight.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightUpdateRequest {

    @NotBlank(message = "Введите город отправления")
    @Size(max = 100, message = "Название города отправления - максимальное кол-во символов 100")
    private String departureCity;

    @NotBlank(message = "Введите город прибытия")
    @Size(max = 100, message = "Название города прибытия - максимальное кол-во символов 100")
    private String arrivalCity;

    @NotNull(message = "Введите дату вылета")
    @Future(message = "Дата вылета должна быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime departureTime;

    @NotNull(message = "Введите дату прибытия")
    @Future(message = "Дата прибытия должна быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arrivalTime;

}
