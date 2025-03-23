package com.example.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "extra_fee", indexes = {
        @Index(name = "idx_condition_type", columnList = "conditionType"),
        @Index(name = "idx_vehicle_type", columnList = "vehicleType")
})
public class ExtraFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String conditionType; // e.g., AIR_TEMPERATURE, WIND_SPEED, WEATHER_PHENOMENON

    @Column(nullable = false)
    private String vehicleType; // e.g., CAR, SCOOTER, BIKE

    private String weatherPhenomenon; // e.g., "snow", "hail"

    private Double fee;

    private Double minValue; // Lower bound of the condition (e.g., -10°C)

    private Double maxValue; // Upper bound of the condition (e.g., 0°C)

    @Column(nullable = false)
    private Boolean isForbidden = Boolean.FALSE;

    public ExtraFee(String vehicleType, String conditionType, Double fee, Double minValue, Double maxValue) {
        this.vehicleType = vehicleType;
        this.conditionType = conditionType;
        this.fee = fee;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public ExtraFee(String conditionType, String vehicleType, String weatherPhenomenon, Double fee,
                    Boolean isForbidden) {
        this.conditionType = conditionType;
        this.vehicleType = vehicleType;
        this.weatherPhenomenon = weatherPhenomenon;
        this.fee = fee;
        this.isForbidden = isForbidden;
    }

    public ExtraFee(String conditionType, String vehicleType, String weatherPhenomenon, Boolean isForbidden) {
        this.conditionType = conditionType;
        this.vehicleType = vehicleType;
        this.weatherPhenomenon = weatherPhenomenon;
        this.isForbidden = isForbidden;
    }
}
