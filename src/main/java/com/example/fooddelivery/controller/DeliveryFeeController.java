package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.DeliveryFeeRequest;
import com.example.fooddelivery.dto.DeliveryFeeResponse;
import com.example.fooddelivery.service.DeliveryFeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery-fee")
public class DeliveryFeeController {

    private final DeliveryFeeService deliveryFeeService;

    public DeliveryFeeController(DeliveryFeeService deliveryFeeService) {
        this.deliveryFeeService = deliveryFeeService;
    }

    @PostMapping
    public ResponseEntity<DeliveryFeeResponse> calculateDeliveryFee(@RequestBody DeliveryFeeRequest request) {
        return ResponseEntity.ok(deliveryFeeService.calculateDeliveryFee(request));
    }
}
