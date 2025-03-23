package com.example.fooddelivery.controller;

import com.example.fooddelivery.entity.ExtraFee;
import com.example.fooddelivery.repository.ExtraFeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class ExtraFeeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExtraFeeRepository extraFeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ExtraFee extraFee;

    @BeforeEach
    void setUp() {
        extraFeeRepository.deleteAllInBatch();
        extraFee = extraFeeRepository.save(
                new ExtraFee(
                        "AIR_TEMPERATURE", "Car", "snow", 2.5,
                        false
                )
        );
    }

    @Test
    void testGetAllExtraFees_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/extra-fee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].conditionType", is("AIR_TEMPERATURE")))
                .andExpect(jsonPath("$[0].vehicleType", is("Car")))
                .andExpect(jsonPath("$[0].weatherPhenomenon", is("snow")))
                .andExpect(jsonPath("$[0].fee", is(2.5)))
                .andExpect(jsonPath("$[0].isForbidden", is(false)));
    }

    @Test
    void testCreateExtraFee_ShouldReturnCreated() throws Exception {
        ExtraFee newExtraFee = new ExtraFee(
                "WIND_SPEED", "Bike", "hail", 3.0, true
        );

        mockMvc.perform(post("/api/extra-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newExtraFee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conditionType", is("WIND_SPEED")))
                .andExpect(jsonPath("$.vehicleType", is("Bike")))
                .andExpect(jsonPath("$.weatherPhenomenon", is("hail")))
                .andExpect(jsonPath("$.fee", is(3.0)))
                .andExpect(jsonPath("$.isForbidden", is(true)));
    }

    @Test
    void testCreateExtraFeesBatch_ShouldReturnCreated() throws Exception {
        List<ExtraFee> extraFees = Arrays.asList(
                new ExtraFee(
                        "WIND_SPEED", "Scooter", "rain", 1.5,
                        false
                ),
                new ExtraFee(
                        "AIR_TEMPERATURE", "Car", "fog", 2.0,
                        true
                )
        );

        mockMvc.perform(post("/api/extra-fee/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(extraFees)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].weatherPhenomenon", is("rain")))
                .andExpect(jsonPath("$[1].isForbidden", is(true)));
    }

    @Test
    void testUpdateExtraFee_ShouldReturnUpdated() throws Exception {
        ExtraFee updatedFee = new ExtraFee(
                "WIND_SPEED", "Bike", "hail", 4.0, true
        );

        mockMvc.perform(put("/api/extra-fee/{id}", extraFee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conditionType", is("WIND_SPEED")))
                .andExpect(jsonPath("$.fee", is(4.0)))
                .andExpect(jsonPath("$.isForbidden", is(true)));
    }

    @Test
    void testUpdateExtraFee_NotFound_ShouldReturnNotFound() throws Exception {
        ExtraFee updatedFee = new ExtraFee(
                "WIND_SPEED", "Bike", "hail", 4.0, true
        );

        mockMvc.perform(put("/api/extra-fee/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFee)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteExtraFee_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/extra-fee/{id}", extraFee.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteExtraFee_NotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/extra-fee/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
