package com.example.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryFeeRequest {
    private String city;
    private String vehicleType;
}