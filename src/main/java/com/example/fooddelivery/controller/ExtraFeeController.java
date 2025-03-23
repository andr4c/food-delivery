package com.example.fooddelivery.controller;

import com.example.fooddelivery.entity.ExtraFee;
import com.example.fooddelivery.exception.ExtraFeeDeletionException;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.repository.ExtraFeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Extra Fee API", description = "API for managing extra fees")
@RestController
@RequestMapping("/api/extra-fee")
public class ExtraFeeController {

    private static final Logger logger = LoggerFactory.getLogger(ExtraFeeController.class);

    private final ExtraFeeRepository extraFeeRepository;

    public ExtraFeeController(ExtraFeeRepository extraFeeRepository) {
        this.extraFeeRepository = extraFeeRepository;
    }

    @Operation(summary = "Get all extra fees", description = "Fetches all the extra fees")
    @GetMapping
    public ResponseEntity<List<ExtraFee>> getAllExtraFees() {
        logger.info("Fetching all extra fees");

        List<ExtraFee> extraFees = extraFeeRepository.findAll();
        return ResponseEntity.ok(extraFees);
    }

    @Operation(summary = "Create a new extra fee", description = "Adds a new extra fee record")
    @PostMapping
    public ResponseEntity<ExtraFee> createExtraFee(@Valid @RequestBody ExtraFee extraFee) {
        logger.info(
                "Creating a new extra fee with conditionType: {}, vehicleType: {}, weatherPhenomenon: {}," +
                        " fee: {}, minValue: {}, maxValue: {}, isForbidden: {}",
                extraFee.getConditionType(), extraFee.getVehicleType(), extraFee.getWeatherPhenomenon(),
                extraFee.getFee(), extraFee.getMinValue(), extraFee.getMaxValue(), extraFee.getIsForbidden()
        );

        ExtraFee savedExtraFee = extraFeeRepository.save(extraFee);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExtraFee); // 201 Created
    }

    @Operation(summary = "Create extra fees in batch", description = "Adds multiple extra fee records")
    @PostMapping("/batch")
    public ResponseEntity<List<ExtraFee>> createExtraFees(@Valid @RequestBody List<ExtraFee> extraFees) {
        logger.info("Creating a batch of {} extra fees", extraFees.size());

        List<ExtraFee> savedFees = extraFeeRepository.saveAll(extraFees);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFees);
    }

    @Operation(summary = "Update an extra fee", description = "Updates an existing extra fee using its ID")
    @PutMapping("/{id}")
    public ResponseEntity<ExtraFee> updateExtraFee(@PathVariable Long id, @Valid @RequestBody ExtraFee updatedExtraFee) {
        logger.info("Updating extra fee with ID: {}", id);

        ExtraFee updatedFee = extraFeeRepository.findById(id)
                .map(extraFee -> {
                    extraFee.setConditionType(updatedExtraFee.getConditionType());
                    extraFee.setVehicleType(updatedExtraFee.getVehicleType());
                    extraFee.setWeatherPhenomenon(updatedExtraFee.getWeatherPhenomenon());
                    extraFee.setFee(updatedExtraFee.getFee());
                    extraFee.setMinValue(updatedExtraFee.getMinValue());
                    extraFee.setMaxValue(updatedExtraFee.getMaxValue());
                    extraFee.setIsForbidden(updatedExtraFee.getIsForbidden());

                    ExtraFee savedFee = extraFeeRepository.save(extraFee);

                    logger.info("Successfully updated extra fee with ID: {}", id);
                    return savedFee;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Extra fee not found with ID: " + id));

        return ResponseEntity.ok(updatedFee);
    }

    @Operation(summary = "Delete an extra fee", description = "Removes an extra fee by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExtraFee(@PathVariable Long id) {
        logger.info("Attempting to delete extra fee with ID: {}", id);

        if (!extraFeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Extra fee not found with ID: " + id);
        }

        try {
            extraFeeRepository.deleteById(id);
            logger.info("Successfully deleted extra fee with ID: {}", id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (Exception ex) {
            logger.error("Error deleting extra fee ID: {} - {}", id, ex.getMessage(), ex);
            throw new ExtraFeeDeletionException("Failed to delete extra fee with ID: " + id);
        }
    }
}
