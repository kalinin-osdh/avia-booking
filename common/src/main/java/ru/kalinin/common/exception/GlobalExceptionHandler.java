package ru.kalinin.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kalinin.common.dto.ErrorResponse;
import ru.kalinin.common.exception.flights.FlightExistsException;
import ru.kalinin.common.exception.refresh_token.RefreshTokenNotValid;
import ru.kalinin.common.exception.seats.SeatAlreadyReservedException;
import ru.kalinin.common.exception.user.UserExistsException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleException(NotFoundException ex){
        return new ErrorResponse("Not found", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleException(AccessDeniedException ex) {
        return new ErrorResponse("Ошибка доступа", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleException(AuthenticationException  ex) {
        return new ErrorResponse("Unauthorized", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleException(BadCredentialsException ex) {
        return new ErrorResponse("Неверные данные при входе", ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenNotValid.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(RefreshTokenNotValid ex){
        return new ErrorResponse("Refresh token is not valid", ex.getMessage());
    }

    @ExceptionHandler(UserExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(UserExistsException ex){
        return new ErrorResponse("Пользователь уже существует", ex.getMessage());
    }

    @ExceptionHandler(FlightExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(FlightExistsException ex){
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

    @ExceptionHandler(SeatAlreadyReservedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(SeatAlreadyReservedException ex){
        return new ErrorResponse("Ошибка бронирования", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex) {
        return new ErrorResponse("Внутренняя ошибка сервера", ex.getMessage());
    }
}
