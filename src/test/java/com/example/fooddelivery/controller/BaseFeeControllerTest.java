package com.example.fooddelivery.controller;

import com.example.fooddelivery.entity.BaseFee;
import com.example.fooddelivery.exception.BaseFeeDeletionException;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.repository.BaseFeeRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseFeeControllerTest {

    @Mock
    private BaseFeeRepository baseFeeRepository;

    private BaseFeeController baseFeeController;

    @BeforeEach
    void setUp() {
        baseFeeController = new BaseFeeController(baseFeeRepository);
    }

    @Test
    void testGetAllBaseFees() {
        List<BaseFee> mockFees = List.of(new BaseFee("Tallinn", "Car", 5.0));
        when(baseFeeRepository.findAll()).thenReturn(mockFees);

        ResponseEntity<List<BaseFee>> response = baseFeeController.getAllBaseFees();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        verify(baseFeeRepository, times(1)).findAll();
    }

    @Test
    void testCreateBaseFee() {
        BaseFee newBaseFee = new BaseFee("Tartu", "Bike", 3.5);
        when(baseFeeRepository.save(any(BaseFee.class))).thenReturn(newBaseFee);

        ResponseEntity<BaseFee> response = baseFeeController.createBaseFee(newBaseFee);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("Tartu", Objects.requireNonNull(response.getBody()).getCity());
        verify(baseFeeRepository, times(1)).save(any(BaseFee.class));
    }

    @Test
    void testCreateExtraFeesBatch_ShouldReturnCreatedList() {
        List<BaseFee> baseFees = Arrays.asList(
                new BaseFee("Tallinn", "Car", 5.0),
                new BaseFee("Tartu", "Bike", 3.5)
        );

        when(baseFeeRepository.saveAll(baseFees)).thenReturn(baseFees);

        ResponseEntity<List<BaseFee>> response = baseFeeController.createBaseFees(baseFees);

        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(baseFeeRepository, times(1)).saveAll(baseFees);
    }

    @Test
    void testUpdateBaseFee_Success() {
        BaseFee existingFee = new BaseFee("Tallinn", "Car", 5.0);
        when(baseFeeRepository.findById(1L)).thenReturn(Optional.of(existingFee));

        BaseFee updatedFee = new BaseFee("Tartu", "Bike", 3.5);
        when(baseFeeRepository.save(any(BaseFee.class))).thenReturn(updatedFee);

        ResponseEntity<BaseFee> response = baseFeeController.updateBaseFee(1L, updatedFee);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Tartu", Objects.requireNonNull(response.getBody()).getCity());
        verify(baseFeeRepository, times(1)).save(any(BaseFee.class));
    }

    @Test
    void testUpdateBaseFee_NotFound() {
        when(baseFeeRepository.findById(1L)).thenReturn(Optional.empty());

        Executable executable = () -> baseFeeController.updateBaseFee(1L, new BaseFee());

        assertThrows(ResourceNotFoundException.class, executable);
    }
    
    @Test
    void testDeleteBaseFee_Success() {
        when(baseFeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(baseFeeRepository).deleteById(1L);

        ResponseEntity<?> response = baseFeeController.deleteBaseFee(1L);

        assertEquals(204, response.getStatusCode().value());
        verify(baseFeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteBaseFee_NotFound() {
        when(baseFeeRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> baseFeeController.deleteBaseFee(1L));
    }

    @Test
    void testDeleteBaseFee_Exception() {
        when(baseFeeRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("DB Error")).when(baseFeeRepository).deleteById(1L);

        assertThrows(BaseFeeDeletionException.class, () -> baseFeeController.deleteBaseFee(1L));
    }

    @Test
    void testCreateBaseFee_InvalidData_ShouldReturnBadRequest() {
        BaseFee invalidBaseFee = new BaseFee("", "", -5.0);

        when(baseFeeRepository.save(any())).thenThrow(ConstraintViolationException.class);

        Executable executable = () -> baseFeeController.createBaseFee(invalidBaseFee);

        Exception exception = assertThrows(Exception.class, executable);
        assertInstanceOf(ConstraintViolationException.class, exception);
    }

    @Test
    void testUpdateBaseFee_InvalidData_ShouldReturnBadRequest() {
        BaseFee existingFee = new BaseFee("Tallinn", "Car", 5.0);
        when(baseFeeRepository.findById(1L)).thenReturn(Optional.of(existingFee));

        BaseFee invalidUpdatedFee = new BaseFee("", "Bike", -2.0);

        when(baseFeeRepository.save(any())).thenThrow(ConstraintViolationException.class);

        Executable executable = () -> baseFeeController.updateBaseFee(1L, invalidUpdatedFee);

        Exception exception = assertThrows(Exception.class, executable);
        assertInstanceOf(ConstraintViolationException.class, exception);
    }
}
