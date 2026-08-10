package ru.kalinin.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.booking.dto.mapper.BookingMapper;
import ru.kalinin.booking.dto.request.BookingRequest;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.entity.Booking;
import ru.kalinin.booking.entity.enums.BookingStatus;
import ru.kalinin.booking.kafka.BookingProducer;
import ru.kalinin.booking.repository.BookingRepository;
import ru.kalinin.booking.service.interfaces.BookingService;
import ru.kalinin.common.exception.bookings.BookingNotFoundException;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final BookingProducer bookingProducer;

    @Override
    public BookingResponse booking(String username, BookingRequest request) {
        Booking saved = bookingRepository.save(bookingMapper.toEntity(request, username));

        bookingProducer.sendBookingCreated(
                BookingCreatedEvent.of(
                        saved.getId(),
                        saved.getBookingNumber(),
                        username,
                        saved.getFlightNumber(),
                        saved.getSeatNumber()
                )
        );

        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserHistory(String username) {
        List<Booking> history = bookingRepository.getBookingsByUsername(username);
        return bookingMapper.toResponse(history);
    }

    @Override
    public boolean confirmBooking(Long id, BigDecimal price) {
        Booking booking = getById(id);

        if (booking.getStatus() != BookingStatus.CREATED)
            return false;

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPrice(price);
        return true;
    }

    @Override
    public boolean declineBooking(Long id) {
        Booking booking = getById(id);

        if (booking.getStatus() != BookingStatus.CREATED)
            return false;

        booking.setStatus(BookingStatus.DECLINED);
        return true;
    }

    @Override
    public boolean successPayment(UUID bookingNumber) {
        Booking booking = getByBookingNumber(bookingNumber);

        if (booking.getStatus() != BookingStatus.CONFIRMED)
            return false;

        booking.setStatus(BookingStatus.PAYMENT_SUCCESS);
        return true;
    }

    @Override
    public boolean failPayment(UUID bookingNumber) {
        Booking booking = getByBookingNumber(bookingNumber);

        if (booking.getStatus() != BookingStatus.CONFIRMED)
            return false;

        booking.setStatus(BookingStatus.PAYMENT_FAILED);
        return true;
    }


    @Override
    @Transactional(readOnly = true)
    public Booking getById(Long id) {
        return bookingRepository.findById(id).orElseThrow(
                () -> new BookingNotFoundException(id)
        );
    }


    @Override
    @Transactional(readOnly = true)
    public Booking getByBookingNumber(UUID bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber).orElseThrow(
                () -> new BookingNotFoundException(bookingNumber)
        );
    }
}
