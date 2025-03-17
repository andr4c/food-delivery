package com.example.fooddelivery.controller;

import com.example.fooddelivery.entity.ExtraFee;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.repository.ExtraFeeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extra-fee")
public class ExtraFeeController {

    private final ExtraFeeRepository extraFeeRepository;

    public ExtraFeeController(ExtraFeeRepository extraFeeRepository) {
        this.extraFeeRepository = extraFeeRepository;
    }

    @GetMapping
    public List<ExtraFee> getAllExtrafees() {
        return extraFeeRepository.findAll();
    }

    @PostMapping
    public ExtraFee createExtraFee(@RequestBody ExtraFee extraFee) {
        return extraFeeRepository.save(extraFee);
    }

    @PutMapping("/{id}")
    public ExtraFee updateExtraFee(@PathVariable Long id, @RequestBody ExtraFee updatedExtraFee) {
        return extraFeeRepository.findById(id).map(extraFee -> {

            extraFee.setConditionType(updatedExtraFee.getConditionType());
            extraFee.setConditionValue(updatedExtraFee.getConditionValue());
            extraFee.setFee(updatedExtraFee.getFee());
            extraFee.setIsForbidden(updatedExtraFee.getIsForbidden());

            return extraFeeRepository.save(extraFee);
        })
                .orElseThrow(() -> new ResourceNotFoundException("Extra fee not found with ID: " + id));
    }

    @DeleteMapping("/{id}")
    public void deleteExtraFee(@PathVariable Long id) {
        extraFeeRepository.deleteById(id);
    }
}
