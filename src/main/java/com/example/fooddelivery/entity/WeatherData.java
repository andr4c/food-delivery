package com.example.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name="weather_data")
public class WeatherData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String stationName;

    @Column(nullable = false)
    private Integer wmoCode;

    @Column(precision = 5, scale = 2)
    private BigDecimal airTemperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal windSpeed;

    private String weatherPhenomenon;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
