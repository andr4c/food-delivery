package com.example.fooddelivery.controller;

import com.example.fooddelivery.entity.BaseFee;
import com.example.fooddelivery.exception.BaseFeeDeletionException;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.repository.BaseFeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Tag(name = "Base Fee API", description = "API for managing base fees")
@RestController
@RequestMapping("/api/base-fee")
public class BaseFeeController {

    private static final Logger logger = LoggerFactory.getLogger(BaseFeeController.class);

    private final BaseFeeRepository baseFeeRepository;

    public BaseFeeController(BaseFeeRepository baseFeeRepository) {
        this.baseFeeRepository = baseFeeRepository;
    }

    @Operation(summary = "Get all base fees", description = "Fetches all the base fees")
    @GetMapping
    public ResponseEntity<List<BaseFee>> getAllBaseFees() {
        logger.info("Fetching all base fees");
        List<BaseFee> baseFees = baseFeeRepository.findAll();
        return ResponseEntity.ok(baseFees);
    }

    @Operation(summary = "Create a new base fee", description = "Adds a new base fee record")
    @PostMapping
    public ResponseEntity<BaseFee> createBaseFee(@Valid @RequestBody BaseFee baseFee) {
        logger.info("Creating a new base fee for city: {}, vehicleType: {}, fee: {}",
                baseFee.getCity(), baseFee.getVehicleType(), baseFee.getFee());

        BaseFee savedBaseFee = baseFeeRepository.save(baseFee);
        return ResponseEntity.status(201).body(savedBaseFee);
    }

    @Operation(summary = "Create base fees in batch", description = "Adds multiple base fee records")
    @PostMapping("/batch")
    public ResponseEntity<List<BaseFee>> createBaseFees(@Valid @RequestBody List<BaseFee> baseFees) {
        logger.info("Creating a batch of {} base fees", baseFees.size());

        List<BaseFee> savedFees = baseFeeRepository.saveAll(baseFees);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFees);
    }

    @Operation(summary = "Update a base fee", description = "Updates an existing base fee using its ID")
    @PutMapping("/{id}")
    public ResponseEntity<BaseFee> updateBaseFee(@PathVariable Long id, @Valid @RequestBody BaseFee newBaseFee) {
        logger.info("Updating base fee with ID: {}", id);

        BaseFee updatedFee = baseFeeRepository.findById(id)
                .map(baseFee -> {
                    baseFee.setCity(newBaseFee.getCity());
                    baseFee.setVehicleType(newBaseFee.getVehicleType());
                    baseFee.setFee(newBaseFee.getFee());
                    return baseFeeRepository.save(baseFee);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Base fee not found with ID: " + id));

        logger.info("Successfully updated base fee with ID: {}", id);
        return ResponseEntity.ok(updatedFee);
    }

    @Operation(summary = "Delete a base fee", description = "Removes a base fee by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBaseFee(@PathVariable Long id) {
        logger.info("Attempting to delete base fee with ID: {}", id);

        if (!baseFeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Base fee not found with ID: " + id);
        }

        try {
            baseFeeRepository.deleteById(id);
            logger.info("Successfully deleted base fee with ID: {}", id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (Exception ex) {
            logger.error("Error deleting base fee ID: {} - {}", id, ex.getMessage(), ex);
            throw new BaseFeeDeletionException("Failed to delete base fee with ID: " + id);
        }
    }
}
