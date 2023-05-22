package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ErrorHandlerController {


    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(final ObjectNotFoundException exception) {
        log.warn("Error! Not Found, server status: '{}' text message: '{}'", HttpStatus.NOT_FOUND, exception.getMessage());
        return Map.of("Not found object", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleNullPointerException(final NullPointerException exception) {
        log.warn("Error! NullPointer, server status: '{}' text message: '{}'",
                HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        return Map.of("Null detected, check your actions", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handlerEmailConflict(final EmailEarlyContains exception) {
        log.warn("Error! Validation fault, server status: '{}' text message: '{}'",
                HttpStatus.CONFLICT, exception.getMessage());
        return Map.of("EMAIL ERROR. ", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handlerValidationException(final ValidationException exception) {
        log.warn("Error! Validation fault, server status: '{}' text message: '{}'",
                HttpStatus.BAD_REQUEST, exception.getMessage());
        return Map.of("Validation fault, check your actions", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handlerValidException(final MethodArgumentNotValidException exception) {
        log.warn("Error! Validation fault, server status: '{}' text message: '{}'",
                HttpStatus.BAD_REQUEST, exception.getMessage());
        return Map.of("Validation object fault, check your actions", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleThrowable(final Throwable exception) {
        log.warn("Error! Server status: '{}' text message: '{}'", HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        return Map.of("Server Error", exception.getMessage());
    }
}