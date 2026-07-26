package ru.kalinin.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kalinin.common.exception.flights.FlightExistsException;
import ru.kalinin.common.exception.model.ErrorResponse;
import ru.kalinin.common.exception.model.NotFoundException;
import ru.kalinin.common.exception.payments.PaymentAlreadyExistsException;
import ru.kalinin.common.exception.payments.PaymentUserNotEqualsException;
import ru.kalinin.common.exception.refresh_token.RefreshTokenNotValid;
import ru.kalinin.common.exception.seats.SeatAlreadyStatusException;
import ru.kalinin.common.exception.users.UserExistsException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleException(NotFoundException ex) {
        return new ErrorResponse("Not found", ex.getMessage());
    }


    @ExceptionHandler(RefreshTokenNotValid.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(RefreshTokenNotValid ex) {
        return new ErrorResponse("Refresh token is not valid", ex.getMessage());
    }

    @ExceptionHandler(UserExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(UserExistsException ex) {
        return new ErrorResponse("Пользователь уже существует", ex.getMessage());
    }

    @ExceptionHandler(FlightExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(FlightExistsException ex) {
        return new ErrorResponse("Полет уже существует", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ErrorResponse("Ошибка валидации", message);
    }

    @ExceptionHandler(SeatAlreadyStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(SeatAlreadyStatusException ex) {
        return new ErrorResponse("Ошибка бронирования", ex.getMessage());
    }

    @ExceptionHandler(PaymentUserNotEqualsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(PaymentUserNotEqualsException ex) {
        return new ErrorResponse("Ошибка оплаты", ex.getMessage());
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(PaymentAlreadyExistsException ex) {
        return new ErrorResponse("Ошибка оплаты", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex) {
        return new ErrorResponse("Внутренняя ошибка сервера", ex.getMessage());
    }
}
