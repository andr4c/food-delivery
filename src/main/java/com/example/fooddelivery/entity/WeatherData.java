package com.example.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@AllArgsConstructor @NoArgsConstructor
@Table(name = "weather_data", indexes = {
        @Index(name = "idx_wmo_code", columnList = "wmoCode"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_station_name", columnList = "stationName")
})
public class WeatherData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String stationName;

    @Column(nullable = false)
    private Integer wmoCode;

    private Double airTemperature;

    private Double windSpeed;

    private String weatherPhenomenon;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public WeatherData(String stationName, Integer wmoCode, Double airTemperature, Double windSpeed,
                       String weatherPhenomenon) {
        this.stationName = stationName;
        this.wmoCode = wmoCode;
        this.airTemperature = airTemperature;
        this.windSpeed = windSpeed;
        this.weatherPhenomenon = weatherPhenomenon;
    }
}
