package com.example.fooddelivery.controller;

import com.example.fooddelivery.entity.BaseFee;
import com.example.fooddelivery.repository.BaseFeeRepository;
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
class BaseFeeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BaseFeeRepository baseFeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private BaseFee baseFee;

    @BeforeEach
    void setUp() {
        baseFeeRepository.deleteAllInBatch();
        baseFeeRepository.flush();
        baseFee = baseFeeRepository.save(new BaseFee("Tallinn", "Car", 5.0));
    }

    @Test
    void testGetAllBaseFees_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/base-fee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].city", is("Tallinn")))
                .andExpect(jsonPath("$[0].vehicleType", is("Car")))
                .andExpect(jsonPath("$[0].fee", is(5.0)));
    }

    @Test
    void testCreateBaseFee_ShouldReturnCreated() throws Exception {
        BaseFee newBaseFee = new BaseFee("Tartu", "Bike", 3.5);

        mockMvc.perform(post("/api/base-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBaseFee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.city", is("Tartu")))
                .andExpect(jsonPath("$.vehicleType", is("Bike")))
                .andExpect(jsonPath("$.fee", is(3.5)));
    }

    @Test
    void testCreateBaseFeesBatch_ValidData_ShouldReturnCreated() throws Exception {
        List<BaseFee> baseFees = Arrays.asList(
                new BaseFee("Tallinn", "Car", 5.0),
                new BaseFee("Tartu", "Bike", 3.5)
        );

        mockMvc.perform(post("/api/base-fee/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseFees)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].city", is("Tallinn")))
                .andExpect(jsonPath("$[1].city", is("Tartu")));
    }

    @Test
    void testUpdateBaseFee_ShouldReturnUpdated() throws Exception {
        BaseFee updatedFee = new BaseFee("Tartu", "Bike", 4.0);

        mockMvc.perform(put("/api/base-fee/{id}", baseFee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("Tartu")))
                .andExpect(jsonPath("$.vehicleType", is("Bike")))
                .andExpect(jsonPath("$.fee", is(4.0)));
    }

    @Test
    void testUpdateBaseFee_NotFound_ShouldReturnNotFound() throws Exception {
        BaseFee updatedFee = new BaseFee("Tartu", "Bike", 4.0);

        mockMvc.perform(put("/api/base-fee/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFee)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBaseFee_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/base-fee/{id}", baseFee.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteBaseFee_NotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/base-fee/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
