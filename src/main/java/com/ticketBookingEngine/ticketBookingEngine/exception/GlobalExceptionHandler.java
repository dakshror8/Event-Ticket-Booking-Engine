package com.ticketBookingEngine.ticketBookingEngine.exception;

import com.ticketBookingEngine.ticketBookingEngine.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDTO> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex,
                                                                           HttpServletRequest request)
    {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message("Seat reservation conflict: The selected seat was just reserved by another user. Please choose a different seat.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
