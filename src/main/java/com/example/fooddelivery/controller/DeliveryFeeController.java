package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.DeliveryFeeRequest;
import com.example.fooddelivery.dto.DeliveryFeeResponse;
import com.example.fooddelivery.exception.DeliveryFeeCalculationException;
import com.example.fooddelivery.exception.InvalidDeliveryFeeRequestException;
import com.example.fooddelivery.service.DeliveryFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Delivery Fee API", description = "API for calculating delivery fees")
@RestController
@RequestMapping("/api/delivery-fee")
public class DeliveryFeeController {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryFeeController.class);

    private final DeliveryFeeService deliveryFeeService;

    public DeliveryFeeController(DeliveryFeeService deliveryFeeService) {
        this.deliveryFeeService = deliveryFeeService;
    }

    @Operation(summary = "Calculate delivery fee",
            description = "Calculates the delivery fee based on request details")
    @PostMapping
    public ResponseEntity<DeliveryFeeResponse> calculateDeliveryFee(@Valid @RequestBody DeliveryFeeRequest request) {
        logger.info("Calculating delivery fee for city: {}, vehicle: {}", request.getCity(), request.getVehicleType());

        if (request.getCity() == null || request.getCity().isBlank() ||
                request.getVehicleType() == null || request.getVehicleType().isBlank()) {
            logger.error("Invalid input: city or vehicleType is empty");
            throw new InvalidDeliveryFeeRequestException("Invalid request: city and vehicleType must not be empty");
        }

        try {
            DeliveryFeeResponse response = deliveryFeeService.calculateDeliveryFee(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error calculating delivery fee: {}", ex.getMessage());
            throw new DeliveryFeeCalculationException("Failed to calculate delivery fee");
        }
    }
}
