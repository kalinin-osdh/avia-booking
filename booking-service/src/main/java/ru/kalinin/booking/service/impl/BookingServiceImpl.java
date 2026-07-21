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
import ru.kalinin.common.exception.booking.BookingNotFoundException;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;
import ru.kalinin.common.kafka.event.EventMetaData;

import java.time.LocalDateTime;
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
        Booking booking = Booking.builder()
                .username(username)
                .flightNumber(request.getFlightNumber())
                .seatNumber(request.getSeatNumber())
                .build();

        Booking saved = bookingRepository.save(booking);

        BookingCreatedEvent event = new BookingCreatedEvent(
                new EventMetaData(
                        UUID.randomUUID(),
                        LocalDateTime.now()
                ),
                saved.getId(),
                username,
                saved.getFlightNumber(),
                saved.getSeatNumber()
        );

        bookingProducer.sendBookingCreated(event);

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserHistory(String username) {
        List<Booking> history = bookingRepository.getBookingsByUsername(username);
        return bookingMapper.toResponse(history);
    }

    @Override
    public void confirmBooking(Long id){
        Booking booking = getById(id);

        if (booking.getStatus() == BookingStatus.CONFIRMED)
            return;

        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Override
    public void declineBooking(Long id){
        Booking booking = getById(id);

        if (booking.getStatus() == BookingStatus.DECLINED)
            return;

        booking.setStatus(BookingStatus.DECLINED);
    }


    @Override
    @Transactional(readOnly = true)
    public Booking getById(Long id){
        return bookingRepository.findById(id).orElseThrow(
                ()-> new BookingNotFoundException(id)
        );
    }
}
