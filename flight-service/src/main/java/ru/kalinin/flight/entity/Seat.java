package ru.kalinin.flight.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.kalinin.flight.entity.enums.SeatStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(name = "price", nullable = false)
    private BigDecimal price;
}
