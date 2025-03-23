package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.DeliveryFeeRequest;
import com.example.fooddelivery.dto.DeliveryFeeResponse;
import com.example.fooddelivery.exception.DeliveryFeeCalculationException;
import com.example.fooddelivery.exception.InvalidDeliveryFeeRequestException;
import com.example.fooddelivery.service.DeliveryFeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryFeeControllerTest {

    @Mock
    private DeliveryFeeService deliveryFeeService;

    @InjectMocks
    private DeliveryFeeController deliveryFeeController;

    private DeliveryFeeRequest request;

    @BeforeEach
    void setUp() {
        request = new DeliveryFeeRequest("Tallinn", "Car");
    }

    @Test
    void testCalculateDeliveryFee_ValidRequest_ShouldReturnFee() {
        DeliveryFeeResponse response = new DeliveryFeeResponse("Success", 5.0);
        when(deliveryFeeService.calculateDeliveryFee(request)).thenReturn(response);

        ResponseEntity<DeliveryFeeResponse> result = deliveryFeeController.calculateDeliveryFee(request);

        assertEquals(5.0, result.getBody().getTotalFee());
        assertEquals("Success", result.getBody().getStatusMessage());
        verify(deliveryFeeService, times(1)).calculateDeliveryFee(request);
    }

    @Test
    void testCalculateDeliveryFee_ServiceThrowsException_ShouldThrowCustomException() {
        when(deliveryFeeService.calculateDeliveryFee(request)).thenThrow(new RuntimeException("Service error"));

        DeliveryFeeCalculationException ex = assertThrows(DeliveryFeeCalculationException.class,
                () -> deliveryFeeController.calculateDeliveryFee(request));

        assertEquals("Failed to calculate delivery fee", ex.getMessage());
        verify(deliveryFeeService, times(1)).calculateDeliveryFee(request);
    }

    @ParameterizedTest
    @CsvSource({
            "'', 'Car'",         // Blank city
            "' ', 'Car'",        // Empty city with whitespace
            "'Tallinn', ''",     // Blank vehicleType
            "'Tallinn', ' '",    // Empty vehicleType with whitespace
            "null, 'Car'",       // Null city
            "'Tallinn', null"    // Null vehicleType
    })
    void testCalculateDeliveryFee_InvalidInput_ShouldThrowValidationException(String city, String vehicleType) {
        DeliveryFeeRequest invalidRequest = new DeliveryFeeRequest(
                "null".equals(city) ? null : city,
                "null".equals(vehicleType) ? null : vehicleType
        );

        InvalidDeliveryFeeRequestException ex = assertThrows(InvalidDeliveryFeeRequestException.class,
                () -> deliveryFeeController.calculateDeliveryFee(invalidRequest));

        assertEquals("Invalid request: city and vehicleType must not be empty", ex.getMessage());
        verify(deliveryFeeService, never()).calculateDeliveryFee(any());
    }
}
