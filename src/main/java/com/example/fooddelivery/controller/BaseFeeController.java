package com.example.fooddelivery.controller;

import com.example.fooddelivery.entity.BaseFee;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.repository.BaseFeeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base-fee")
public class BaseFeeController {

    private final BaseFeeRepository baseFeeRepository;

    public BaseFeeController(BaseFeeRepository baseFeeRepository) {
        this.baseFeeRepository = baseFeeRepository;
    }

    @GetMapping
    public List<BaseFee> getAllBaseFees() {
        return baseFeeRepository.findAll();
    }

    @PostMapping
    public BaseFee createBaseFee(@RequestBody BaseFee baseFee) {
        return baseFeeRepository.save(baseFee);
    }

    @PutMapping("/{id}")
    public BaseFee updateBaseFee(@PathVariable Long id, @RequestBody BaseFee newBaseFee) {
        return baseFeeRepository.findById(id).map(baseFee -> {

            baseFee.setCity(newBaseFee.getCity());
            baseFee.setVehicleType(newBaseFee.getVehicleType());
            baseFee.setFee(newBaseFee.getFee());

            return baseFeeRepository.save(baseFee);

        })
                .orElseThrow(() -> new ResourceNotFoundException("Base fee not found with ID: " + id));
    }

    @DeleteMapping("/{id}")
    public void deleteBaseFee(@PathVariable Long id) {
        baseFeeRepository.deleteById(id);
    }
}
