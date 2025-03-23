package com.example.fooddelivery.exception;

public class BaseFeeNotFoundException extends RuntimeException {
    public BaseFeeNotFoundException(String message) {
        super(message);
    }
}
