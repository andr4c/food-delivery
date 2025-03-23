package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.DeliveryFeeRequest;
import com.example.fooddelivery.dto.DeliveryFeeResponse;
import com.example.fooddelivery.exception.DeliveryFeeCalculationException;
import com.example.fooddelivery.service.DeliveryFeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DeliveryFeeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliveryFeeService deliveryFeeService;

    private DeliveryFeeRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new DeliveryFeeRequest("Tallinn", "Car");
    }

    @Test
    void testCalculateDeliveryFee_ValidRequest_ShouldReturnFee() throws Exception {
        DeliveryFeeResponse response = new DeliveryFeeResponse("Success", 5.0);
        when(deliveryFeeService.calculateDeliveryFee(any(DeliveryFeeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/delivery-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFee").value(5.0))
                .andExpect(jsonPath("$.statusMessage").value("Success"));

        verify(deliveryFeeService, times(1)).calculateDeliveryFee(any(DeliveryFeeRequest.class));
    }

    @Test
    void testCalculateDeliveryFee_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        DeliveryFeeRequest invalidRequest = new DeliveryFeeRequest("", "");

        mockMvc.perform(post("/api/delivery-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())  // Expecting 400 now
                .andExpect(jsonPath("$.message").value(
                        "Invalid request: city and vehicleType must not be empty"
                ))
                .andExpect(jsonPath("$.details").value("Invalid input data"));

        verify(deliveryFeeService, never()).calculateDeliveryFee(any());
    }

    @Test
    void testCalculateDeliveryFee_ServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(deliveryFeeService.calculateDeliveryFee(any(DeliveryFeeRequest.class)))
                .thenThrow(new DeliveryFeeCalculationException("Calculation failed"));

        mockMvc.perform(post("/api/delivery-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to calculate delivery fee"));

        verify(deliveryFeeService, times(1)).calculateDeliveryFee(any(DeliveryFeeRequest.class));
    }

    @Test
    void testCalculateDeliveryFee_MissingFields_ShouldReturnBadRequest() throws Exception {
        DeliveryFeeRequest missingFieldsRequest = new DeliveryFeeRequest(null, null);

        mockMvc.perform(post("/api/delivery-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingFieldsRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid request: city and vehicleType must not be empty"));

        verify(deliveryFeeService, never()).calculateDeliveryFee(any(DeliveryFeeRequest.class));
    }

    @Test
    void testCalculateDeliveryFee_EmptyCity_ShouldReturnBadRequest() throws Exception {
        DeliveryFeeRequest invalidRequest = new DeliveryFeeRequest("", "Car");

        mockMvc.perform(post("/api/delivery-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid request: city and vehicleType must not be empty"));

        verify(deliveryFeeService, never()).calculateDeliveryFee(any(DeliveryFeeRequest.class));
    }

    @Test
    void testCalculateDeliveryFee_EmptyVehicleType_ShouldReturnBadRequest() throws Exception {
        DeliveryFeeRequest invalidRequest = new DeliveryFeeRequest("Tallinn", "");

        mockMvc.perform(post("/api/delivery-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid request: city and vehicleType must not be empty"));

        verify(deliveryFeeService, never()).calculateDeliveryFee(any(DeliveryFeeRequest.class));
    }
}
