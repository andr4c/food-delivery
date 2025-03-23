package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    Boolean existsByWmoCodeAndTimestamp(Integer wmoCode, LocalDateTime timestamp);

    @Query("SELECT w FROM WeatherData w WHERE LOWER(w.stationName) LIKE LOWER(CONCAT('%', :stationName, '%'))")
    Optional<WeatherData> findByStationNameContaining(@Param("stationName") String stationName);
}
