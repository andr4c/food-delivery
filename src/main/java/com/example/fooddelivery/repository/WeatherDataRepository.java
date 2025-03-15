package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    @Query("SELECT COUNT(w) FROM WeatherData w WHERE w.wmoCode = :wmoCode AND w.timestamp = :timestamp")
    long countByWmoCodeAndTimestamp(@Param("wmoCode") Integer wmoCode, @Param("timestamp") LocalDateTime timestamp);
}
