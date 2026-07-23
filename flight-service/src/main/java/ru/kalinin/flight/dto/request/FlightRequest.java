package ru.kalinin.flight.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kalinin.flight.entity.Seat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightRequest {

    @NotBlank(message = "Введите номер полета")
    @Size(max = 20, message = "Номер полета должен быть от 0 до 20 символов")
    private String flightNumber;

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
