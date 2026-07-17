package ru.kalinin.flight.dto.request;

import com.fasterxml.jackson.databind.annotation.EnumNaming;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kalinin.flight.entity.enums.SeatStatus;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatRequest {

    @NotNull(message = "Введите ID полета, которому принадлежит место")
    @Positive(message = "ID не может быть <0")
    private Long flightId;

    @NotBlank(message = "Введите номер места")
    @Size(max = 10, message = "Номер места должен быть от 0 до 10 символов")
    private String seatNumber;

    @NotNull(message = "Введите статус места")
    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @NotNull(message = "Введите стоимость места")
    @DecimalMin(value = "500.00", message = "Билет не может стоить меньше 500.00 рублей")
    @DecimalMax(value = "250000.00", message = "Билет не может стоить дороже 250 тысяч рублей")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
}
