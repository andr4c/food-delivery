package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.ExtraFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtraFeeRepository extends JpaRepository<ExtraFee, Long> {
    List<ExtraFee> findByConditionTypeAndVehicleType(String conditionType, String vehicleType);
}