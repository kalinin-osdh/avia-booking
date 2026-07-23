package ru.kalinin.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.kalinin.booking.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_number", nullable = false, unique = true)
    @Builder.Default
    private UUID bookingNumber = UUID.randomUUID();

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "flight_number", nullable = false)
    private String flightNumber;

    @Column(name = "seat_number", nullable = false)
    private String  seatNumber;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingStatus status = BookingStatus.CREATED;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
