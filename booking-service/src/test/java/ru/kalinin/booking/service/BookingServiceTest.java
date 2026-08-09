package ru.kalinin.booking.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kalinin.booking.dto.mapper.BookingMapper;
import ru.kalinin.booking.dto.request.BookingRequest;
import ru.kalinin.booking.dto.response.BookingResponse;
import ru.kalinin.booking.entity.Booking;
import ru.kalinin.booking.entity.enums.BookingStatus;
import ru.kalinin.booking.factory.TestDataFactory;
import ru.kalinin.booking.kafka.BookingProducer;
import ru.kalinin.booking.repository.BookingRepository;
import ru.kalinin.booking.service.impl.BookingServiceImpl;
import ru.kalinin.common.exception.bookings.BookingNotFoundException;
import ru.kalinin.common.kafka.event.booking.BookingCreatedEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("booking-service")
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingProducer bookingProducer;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    @DisplayName("Должен создать новую запись о бронировании")
    void shouldBookFlightSeat() {
        BookingRequest request = TestDataFactory.createBookingRequest("1A", "1S");
        Booking booking = TestDataFactory.createBooking(1L, request);
        BookingResponse expectedResponse = TestDataFactory.createBookingResponse(booking);

        when(bookingMapper.toEntity(request, "kalinin")).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toResponse(booking)).thenReturn(expectedResponse);

        BookingResponse actualResponse = bookingService.booking("kalinin", request);

        ArgumentCaptor<BookingCreatedEvent> captor = ArgumentCaptor.forClass(BookingCreatedEvent.class);

        verify(bookingProducer).sendBookingCreated(captor.capture());

        BookingCreatedEvent event = captor.getValue();

        assertThat(event).isNotNull()
                .satisfies(e -> {
                    assertThat(e.metadata()).isNotNull();
                    assertThat(e.bookingId()).isEqualTo(booking.getId());
                    assertThat(e.bookingNumber()).isEqualTo(expectedResponse.getBookingNumber());
                    assertThat(e.username()).isEqualTo(booking.getUsername());
                    assertThat(e.flightNumber()).isEqualTo(booking.getFlightNumber());
                    assertThat(e.seatNumber()).isEqualTo(booking.getSeatNumber());
                });

        assertThat(actualResponse).isNotNull()
                .isEqualTo(expectedResponse);

        verify(bookingMapper).toEntity(request, "kalinin");
        verify(bookingRepository).save(booking);
        verify(bookingMapper).toResponse(booking);
    }

    @Test
    @DisplayName("Должен вернуть история бронирования пользователя")
    void shouldGetUserHistory() {
        List<Booking> bookings = List.of(
                TestDataFactory.createBooking(1L, "1A", "1S"),
                TestDataFactory.createBooking(2L, "1A", "2S")
        );
        List<BookingResponse> expectedResponse = List.of(
                TestDataFactory.createBookingResponse(bookings.get(0)),
                TestDataFactory.createBookingResponse(bookings.get(1))
        );

        when(bookingRepository.getBookingsByUsername("kalinin")).thenReturn(bookings);
        when(bookingMapper.toResponse(bookings)).thenReturn(expectedResponse);

        List<BookingResponse> actualResponse = bookingService.getUserHistory("kalinin");

        assertThat(actualResponse).isNotNull()
                .hasSize(2)
                .extracting(BookingResponse::getBookingNumber)
                .containsExactly(
                        expectedResponse.get(0).getBookingNumber(),
                        expectedResponse.get(1).getBookingNumber()
                );

        verify(bookingRepository).getBookingsByUsername("kalinin");
        verify(bookingMapper).toResponse(bookings);
    }

    @Test
    @DisplayName("Должен изменить статус бронирования и установить цену при его подтверждении")
    void shouldConfirmBooking() {
        Booking booking = TestDataFactory.createBooking(1L, "1A", "1S");

        BigDecimal price = BigDecimal.valueOf(1000.00);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CREATED);
        assertThat(booking.getPrice()).isNull();

        bookingService.confirmBooking(1L, price);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getPrice()).isEqualByComparingTo(price);

        verify(bookingRepository).findById(1L);
    }

    @Test
    @DisplayName("Должен выбросить BookingNotFoundException при подтверждении бронирования")
    void shouldThrowBookingNotFoundExceptionWhenConfirmingBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.confirmBooking(1L,BigDecimal.valueOf(1000)))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("Запись о бронировании не найдена: " + 1L);

        verify(bookingRepository).findById(1L);
    }

    @Test
    @DisplayName("Должен изменить статус бронирования при его отклонении")
    void shouldDeclineBooking() {
        Booking booking = TestDataFactory.createBooking(1L, "1A", "1S");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CREATED);
        assertThat(booking.getPrice()).isNull();

        bookingService.declineBooking(1L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.DECLINED);
        assertThat(booking.getPrice()).isNull();

        verify(bookingRepository).findById(1L);
    }

    @Test
    @DisplayName("Должен выбросить BookingNotFoundException при отклонении бронирования")
    void shouldThrowBookingNotFoundExceptionWhenDecliningBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.declineBooking(1L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("Запись о бронировании не найдена: " + 1L);

        verify(bookingRepository).findById(1L);
    }

    @Test
    @DisplayName("Должен изменить статус бронирования при подтверждении оплаты")
    void shouldConfirmPayment() {
        Booking booking = TestDataFactory.createBooking(1L, "1A", "1S");
        booking.setStatus(BookingStatus.CONFIRMED);
        BigDecimal price = BigDecimal.valueOf(1000.00);
        booking.setPrice(price);

        when(bookingRepository.findByBookingNumber(booking.getBookingNumber())).thenReturn(Optional.of(booking));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getPrice()).isNotNull();

        bookingService.successPayment(booking.getBookingNumber());

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAYMENT_SUCCESS);
        assertThat(booking.getPrice()).isEqualByComparingTo(price);

        verify(bookingRepository).findByBookingNumber(booking.getBookingNumber());
    }

    @Test
    @DisplayName("Должен выбросить BookingNotFoundException при подтверждении оплаты бронирования")
    void shouldThrowBookingNotFoundExceptionWhenPaymentSuccess() {
        UUID bookingNumber = UUID.randomUUID();

        when(bookingRepository.findByBookingNumber(bookingNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.successPayment(bookingNumber))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("Запись о бронировании не найдена: " + bookingNumber);

        verify(bookingRepository).findByBookingNumber(bookingNumber);
    }

    @Test
    @DisplayName("Должен изменить статус бронирования при отклонении оплаты")
    void shouldDeclinePayment() {
        Booking booking = TestDataFactory.createBooking(1L, "1A", "1S");
        booking.setStatus(BookingStatus.CONFIRMED);
        BigDecimal price = BigDecimal.valueOf(1000.00);
        booking.setPrice(price);

        when(bookingRepository.findByBookingNumber(booking.getBookingNumber())).thenReturn(Optional.of(booking));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getPrice()).isNotNull();

        bookingService.failPayment(booking.getBookingNumber());

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAYMENT_FAILED);
        assertThat(booking.getPrice()).isEqualByComparingTo(price);

        verify(bookingRepository).findByBookingNumber(booking.getBookingNumber());
    }

    @Test
    @DisplayName("Должен выбросить BookingNotFoundException при отклонении оплаты бронирования")
    void shouldThrowBookingNotFoundExceptionWhenPaymentFailed() {
        UUID bookingNumber = UUID.randomUUID();

        when(bookingRepository.findByBookingNumber(bookingNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.failPayment(bookingNumber))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("Запись о бронировании не найдена: " + bookingNumber);

        verify(bookingRepository).findByBookingNumber(bookingNumber);
    }

    @Test
    @DisplayName("Должен вернуть booking по id")
    void shouldReturnBookingById() {
        Booking booking = TestDataFactory.createBooking(1L, "1A", "1S");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Booking actualBooking = bookingService.getById(1L);

        assertThat(actualBooking)
                .isNotNull()
                .isEqualTo(booking);

        verify(bookingRepository).findById(1L);
    }

    @Test
    @DisplayName("Должен вернуть 404 BookingNotFoundException когда booking не найден по id")
    void shouldThrowBookingNotFoundExceptionWhenBookingNotFoundById() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(1L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("Запись о бронировании не найдена: " + 1L);

        verify(bookingRepository).findById(1L);
    }

    @Test
    @DisplayName("Должен вернуть booking по его номеру")
    void shouldReturnBookingByBookingNumber() {
        Booking booking = TestDataFactory.createBooking(1L, "1A", "1S");

        when(bookingRepository.findByBookingNumber(booking.getBookingNumber())).thenReturn(Optional.of(booking));

        Booking actualBooking = bookingService.getByBookingNumber(booking.getBookingNumber());

        assertThat(actualBooking)
                .isNotNull()
                .isEqualTo(booking);

        verify(bookingRepository).findByBookingNumber(booking.getBookingNumber());
    }

    @Test
    @DisplayName("Должен вернуть 404 BookingNotFoundException когда booking не найден по его номеру")
    void shouldThrowBookingNotFoundExceptionWhenBookingNotFoundByBookingNumber() {
        UUID bookingNumber = UUID.randomUUID();

        when(bookingRepository.findByBookingNumber(bookingNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getByBookingNumber(bookingNumber))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("Запись о бронировании не найдена: " + bookingNumber);

        verify(bookingRepository).findByBookingNumber(bookingNumber);
    }
}
