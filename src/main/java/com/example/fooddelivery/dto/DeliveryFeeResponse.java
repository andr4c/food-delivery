package com.example.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryFeeResponse {
    private String message;
    private Double totalFee;
}
