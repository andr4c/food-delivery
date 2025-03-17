package com.example.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@Entity
@Table(name = "extra_fee")
public class ExtraFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conditionType;
    private String conditionValue;
    private BigDecimal fee;
    private Boolean isForbidden;
}
