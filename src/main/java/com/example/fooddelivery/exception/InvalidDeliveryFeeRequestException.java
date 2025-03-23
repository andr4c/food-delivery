package com.example.fooddelivery.exception;

public class InvalidDeliveryFeeRequestException extends RuntimeException {
    public InvalidDeliveryFeeRequestException(String message) {
        super(message);
    }
}
