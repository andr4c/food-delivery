package com.example.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "base_fee", indexes = {
        @Index(name = "idx_city_vehicle", columnList = "city, vehicleType")
})
public class BaseFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String vehicleType;

    @Column(nullable = false)
    private Double fee;

    public BaseFee(String city, String vehicleType, Double fee) {
        this.city = city;
        this.vehicleType = vehicleType;
        this.fee = fee;
    }
}
