package com.example.fooddelivery.dto;

import lombok.Data;

@Data
public class DeliveryFeeRequest {
    private String city;
    private String vehicleType;
}