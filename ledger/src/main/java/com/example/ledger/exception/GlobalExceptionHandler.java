package com.example.ledger.exception;



import com.example.ledger.api.dto.ApiError;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Request validation failed");

        return new ApiError(
                message,
                Instant.now()
        );
    }

    @ExceptionHandler({
            InvalidMovementException.class,
            InsufficientFundsException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusinessException(
            RuntimeException exception
    ) {
        return new ApiError(
                exception.getMessage(),
                Instant.now()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUnreadableRequest(
            HttpMessageNotReadableException exception
    ) {
        return new ApiError(
                "Request body is invalid",
                Instant.now()
        );
    }
}