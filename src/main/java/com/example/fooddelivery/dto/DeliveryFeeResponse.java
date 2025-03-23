package com.example.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter @Setter
@AllArgsConstructor
public class DeliveryFeeResponse {
    private String statusMessage;
    private Double totalFee;
}
