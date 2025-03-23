package com.example.fooddelivery.controller;

import com.example.fooddelivery.entity.ExtraFee;
import com.example.fooddelivery.exception.ExtraFeeDeletionException;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.repository.ExtraFeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtraFeeControllerTest {

    @Mock
    private ExtraFeeRepository extraFeeRepository;

    @InjectMocks
    private ExtraFeeController extraFeeController;

    private ExtraFee extraFee;

    @BeforeEach
    void setUp() {
        extraFee = new ExtraFee();
        extraFee.setId(1L);
        extraFee.setConditionType("air_temperature");
        extraFee.setVehicleType("Car");
        extraFee.setWeatherPhenomenon("Heavy Rain");
        extraFee.setFee(2.5);
        extraFee.setMinValue(0.0);
        extraFee.setMaxValue(10.0);
        extraFee.setIsForbidden(false);
    }

    @Test
    void testGetAllExtraFees_ShouldReturnList() {
        List<ExtraFee> feeList = Collections.singletonList(extraFee);
        when(extraFeeRepository.findAll()).thenReturn(feeList);

        ResponseEntity<List<ExtraFee>> response = extraFeeController.getAllExtraFees();

        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        assertEquals("air_temperature", response.getBody().get(0).getConditionType());
        verify(extraFeeRepository, times(1)).findAll();
    }

    @Test
    void testCreateExtraFee_ShouldReturnCreatedFee() {
        when(extraFeeRepository.save(any(ExtraFee.class))).thenReturn(extraFee);

        ResponseEntity<ExtraFee> response = extraFeeController.createExtraFee(extraFee);

        assertNotNull(response.getBody());
        assertEquals(2.5, response.getBody().getFee());
        verify(extraFeeRepository, times(1)).save(extraFee);
    }

    @Test
    void testCreateExtraFeesBatch_ShouldReturnCreatedList() {
        List<ExtraFee> extraFees = Arrays.asList(extraFee, extraFee);
        when(extraFeeRepository.saveAll(extraFees)).thenReturn(extraFees);

        ResponseEntity<List<ExtraFee>> response = extraFeeController.createExtraFees(extraFees);

        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(extraFeeRepository, times(1)).saveAll(extraFees);
    }

    @Test
    void testUpdateExtraFee_ShouldReturnUpdatedFee() {
        ExtraFee updatedFee = new ExtraFee();
        updatedFee.setConditionType("Snow");
        updatedFee.setFee(3.0);

        when(extraFeeRepository.findById(1L)).thenReturn(Optional.of(extraFee));
        when(extraFeeRepository.save(any(ExtraFee.class))).thenReturn(updatedFee);

        ResponseEntity<ExtraFee> response = extraFeeController.updateExtraFee(1L, updatedFee);

        assertEquals("Snow", Objects.requireNonNull(response.getBody()).getConditionType());
        assertEquals(3.0, response.getBody().getFee());
        verify(extraFeeRepository, times(1)).findById(1L);
        verify(extraFeeRepository, times(1)).save(extraFee);
    }

    @Test
    void testUpdateExtraFee_NotFound_ShouldThrowException() {
        when(extraFeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            extraFeeController.updateExtraFee(1L, extraFee);
        });

        verify(extraFeeRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteExtraFee_ShouldReturnNoContent() {
        when(extraFeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(extraFeeRepository).deleteById(1L);

        ResponseEntity<Void> response = extraFeeController.deleteExtraFee(1L);

        assertEquals(204, response.getStatusCode().value());
        verify(extraFeeRepository, times(1)).existsById(1L);
        verify(extraFeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteExtraFee_NotFound_ShouldThrowException() {
        when(extraFeeRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            extraFeeController.deleteExtraFee(1L);
        });

        verify(extraFeeRepository, times(1)).existsById(1L);
    }

    @Test
    void testDeleteExtraFee_Exception_ShouldThrowDeletionException() {
        when(extraFeeRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(extraFeeRepository).deleteById(1L);

        assertThrows(ExtraFeeDeletionException.class, () -> {
            extraFeeController.deleteExtraFee(1L);
        });

        verify(extraFeeRepository, times(1)).existsById(1L);
        verify(extraFeeRepository, times(1)).deleteById(1L);
    }
}
