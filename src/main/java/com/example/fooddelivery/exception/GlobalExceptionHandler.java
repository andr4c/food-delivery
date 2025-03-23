package com.example.fooddelivery.exception;

import com.example.fooddelivery.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidDeliveryFeeRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDeliveryFeeRequestException(
            InvalidDeliveryFeeRequestException ex) {
        logger.warn("Invalid request: {}", ex.getMessage());
        return errorResponseBuilder(ex.getMessage(), "Invalid input data", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BaseFeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBaseFeeNotFound(BaseFeeNotFoundException ex) {
        logger.warn("Base fee not found: {}", ex.getMessage());
        return errorResponseBuilder(ex.getMessage(), "Base fee not found.",
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DeliveryFeeCalculationException.class)
    public ResponseEntity<ErrorResponse> handleDeliveryFeeCalculationException(DeliveryFeeCalculationException ex) {
        logger.warn("Failed to calculate delivery fee: {}", ex.getMessage());
        return errorResponseBuilder(ex.getMessage(), "Failed to calculate delivery fee",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExtraFeeDeletionException.class)
    public ResponseEntity<ErrorResponse> handleExtraFeeDeletionException(ExtraFeeDeletionException ex) {
        logger.warn("Extra fee deletion not successful: {}", ex.getMessage());
        return errorResponseBuilder(ex.getMessage(), "Extra fee deletion not successful",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BaseFeeDeletionException.class)
    public ResponseEntity<ErrorResponse> handleBaseFeeDeletionException(BaseFeeDeletionException ex) {
        logger.warn("Base fee deletion not successful: {}", ex.getMessage());
        return errorResponseBuilder(ex.getMessage(), "Base fee deletion not successful",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return errorResponseBuilder(ex.getMessage(), "Resource not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidVehicleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVehicleException(InvalidVehicleException ex) {
        logger.warn("Invalid vehicle type: {}", ex.getMessage());
        return errorResponseBuilder(ex.getMessage(), "Usage of selected vehicle type is forbidden", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid input data: {}", ex.getMessage());
        return errorResponseBuilder(ex.getMessage(), "Invalid input data", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        logger.error("Unexpected server error: {}", ex.getMessage(), ex);
        return errorResponseBuilder("An unexpected error occurred", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
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
