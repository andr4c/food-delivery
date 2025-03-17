package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.BaseFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaseFeeRepository extends JpaRepository<BaseFee, Long> {
    Optional<BaseFee> findByCityAndVehicleType(String city, String vehicleType);
}
