package com.example.fooddelivery.exception;

import com.example.fooddelivery.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return errorResponseBuilder(ex.getMessage(),"Resource not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidVehicleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVehicleException(InvalidVehicleException ex) {
        return errorResponseBuilder(ex.getMessage(), "Vehicle restriction due to weather conditions",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return errorResponseBuilder(ex.getMessage(), "Invalid input data",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        return errorResponseBuilder(ex.getMessage(), "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> errorResponseBuilder(String message, String details, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                message,
                details,
                status.value()
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}
