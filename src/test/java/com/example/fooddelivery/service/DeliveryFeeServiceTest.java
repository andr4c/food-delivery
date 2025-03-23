package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.DeliveryFeeRequest;
import com.example.fooddelivery.dto.DeliveryFeeResponse;
import com.example.fooddelivery.entity.BaseFee;
import com.example.fooddelivery.entity.ExtraFee;
import com.example.fooddelivery.entity.WeatherData;
import com.example.fooddelivery.exception.BaseFeeNotFoundException;
import com.example.fooddelivery.exception.InvalidVehicleException;
import com.example.fooddelivery.repository.BaseFeeRepository;
import com.example.fooddelivery.repository.ExtraFeeRepository;
import com.example.fooddelivery.repository.WeatherDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeliveryFeeServiceTest {

    private BaseFeeRepository baseFeeRepository;
    private ExtraFeeRepository extraFeeRepository;
    private WeatherDataRepository weatherDataRepository;
    private DeliveryFeeService deliveryFeeService;

    @BeforeEach
    void setUp() {
        baseFeeRepository = mock(BaseFeeRepository.class);
        extraFeeRepository = mock(ExtraFeeRepository.class);
        weatherDataRepository = mock(WeatherDataRepository.class);
        deliveryFeeService = new DeliveryFeeService(baseFeeRepository, extraFeeRepository, weatherDataRepository);

        when(baseFeeRepository.findByCityAndVehicleType("Tallinn", "Car"))
                .thenReturn(Optional.of(new BaseFee("Tallinn", "Car", 4.0)));

        when(weatherDataRepository.findByStationNameContaining("Tallinn"))
                .thenReturn(Optional.of(
                        new WeatherData(
                                "Tallinn", 23503, -12.0, 5.0,
                                "clear"
                        )
                ));
    }

    @ParameterizedTest
    @CsvSource({
            "Tallinn, Car, 4.0",
            "Tartu, Scooter, 3.5",
            "Parnu, Bike, 2.5"
    })
    void testGetBaseFee_ValidCases(String city, String vehicleType, double expectedFee) {
        when(baseFeeRepository.findByCityAndVehicleType(city, vehicleType))
                .thenReturn(Optional.of(new BaseFee(city, vehicleType, expectedFee)));

        assertEquals(expectedFee, deliveryFeeService.getBaseFee(city, vehicleType));
    }

    @Test
    void testGetBaseFee_NotFound_ThrowsException() {
        when(baseFeeRepository.findByCityAndVehicleType("UnknownCity", "Car"))
                .thenReturn(Optional.empty());

        assertThrows(BaseFeeNotFoundException.class, () ->
                deliveryFeeService.getBaseFee("UnknownCity", "Car"));
    }

    @ParameterizedTest
    @CsvSource({
            "Scooter, -11.0, 1.0",
            "Scooter, -9.0, 0.5",
            "Bike, -5.0, 0.5",
            "Bike, 2.0, 0.0"
    })
    void testGetAirTemperatureFee(String vehicleType, double airTemperature, double expectedFee) {
        when(extraFeeRepository.findByConditionTypeAndVehicleType("air_temperature", vehicleType))
                .thenReturn(List.of(
                        new ExtraFee(
                                "air_temperature", vehicleType, 1.0, -100.0, -10.0
                        ),
                        new ExtraFee(
                                "air_temperature", vehicleType, 0.5, -10.0, 0.0
                        )
                ));

        assertEquals(expectedFee, deliveryFeeService.getAirTemperatureFee(vehicleType, airTemperature));
    }

    @ParameterizedTest
    @CsvSource({
            "Bike, 15.0, 0.5",
            "Bike, 25.0, 0.0"
    })
    void testGetWindSpeedFee(String vehicleType, double windSpeed, double expectedFee) {
        when(extraFeeRepository.findByConditionTypeAndVehicleType("wind_speed", vehicleType))
                .thenReturn(List.of(
                        new ExtraFee("wind_speed", vehicleType, 0.5, 10.0, 20.0)
                ));

        if (windSpeed > 20) {
            assertThrows(InvalidVehicleException.class,
                    () -> deliveryFeeService.getWindSpeedFee(vehicleType, windSpeed));
        } else {
            assertEquals(expectedFee, deliveryFeeService.getWindSpeedFee(vehicleType, windSpeed));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "Scooter, snow, 1.0",
            "Bike, rain, 0.5",
            "Scooter, hail, 0.0"
    })
    void testGetWeatherPhenomenonFee(String vehicleType, String phenomenon, double expectedFee) {
        when(extraFeeRepository.findByConditionTypeAndVehicleType("weather_phenomenon", vehicleType))
                .thenReturn(List.of(
                        new ExtraFee(
                                "weather_phenomenon", vehicleType, "snow", 1.0,
                                false
                        ),
                        new ExtraFee("weather_phenomenon", vehicleType, "rain", 0.5,
                                false
                        ),
                        new ExtraFee("weather_phenomenon", vehicleType, "hail",
                                true
                        )
                ));

        if (phenomenon.equals("hail")) {
            assertThrows(InvalidVehicleException.class,
                    () -> deliveryFeeService.getWeatherPhenomenonFee(vehicleType, phenomenon));
        } else {
            assertEquals(expectedFee, deliveryFeeService.getWeatherPhenomenonFee(vehicleType, phenomenon));
        }
    }

    @Test
    void testCalculateDeliveryFee_Success() {
        DeliveryFeeRequest request = new DeliveryFeeRequest("Tallinn", "Car");

        when(baseFeeRepository.findByCityAndVehicleType("Tallinn", "Car"))
                .thenReturn(Optional.of(new BaseFee("Tallinn", "Car", 4.0)));

        when(extraFeeRepository.findByConditionTypeAndVehicleType(any(), any()))
                .thenReturn(List.of());

        DeliveryFeeResponse response = deliveryFeeService.calculateDeliveryFee(request);
        assertEquals(4.0, response.getTotalFee());
    }

    @Test
    void testCalculateDeliveryFee_WithExtraFees() {
        DeliveryFeeRequest request = new DeliveryFeeRequest("Tallinn", "Scooter");

        when(baseFeeRepository.findByCityAndVehicleType("Tallinn", "Scooter"))
                .thenReturn(Optional.of(new BaseFee("Tallinn", "Scooter", 3.0)));

        when(weatherDataRepository.findByStationNameContaining("Tallinn"))
                .thenReturn(Optional.of(
                        new WeatherData(
                                "Tallinn", 23503, -12.0, 5.0,
                                "snow"
                        )
                ));

        when(extraFeeRepository.findByConditionTypeAndVehicleType("air_temperature", "Scooter"))
                .thenReturn(List.of(
                        new ExtraFee(
                                "air_temperature", "Scooter", 1.0, -100.0,
                                -10.0
                        )
                ));

        when(extraFeeRepository.findByConditionTypeAndVehicleType(
                "weather_phenomenon", "Scooter"
        ))
                .thenReturn(List.of(
                        new ExtraFee("weather_phenomenon", "Scooter", "snow",
                                1.0, false
                        )
                ));

        DeliveryFeeResponse response = deliveryFeeService.calculateDeliveryFee(request);
        assertEquals(5.0, response.getTotalFee()); // 3.0 (base) + 1.0 (temperature) + 1.0 (snow)
    }

    @Test
    void testCalculateDeliveryFee_ForbiddenVehicle() {
        DeliveryFeeRequest request = new DeliveryFeeRequest("Tallinn", "Bike");

        when(baseFeeRepository.findByCityAndVehicleType("Tallinn", "Bike"))
                .thenReturn(Optional.of(new BaseFee("Tallinn", "Bike", 3.0)));

        when(weatherDataRepository.findByStationNameContaining("Tallinn"))
                .thenReturn(Optional.of(new WeatherData(
                        "Tallinn", 23503, 5.0, 25.0,
                        "clear"
                )));

        when(extraFeeRepository.findByConditionTypeAndVehicleType("wind_speed", "Bike"))
                .thenReturn(List.of(new ExtraFee(
                        "wind_speed", "Bike", 0.5, 10.0, 20.0
                )));

        assertThrows(InvalidVehicleException.class, () -> deliveryFeeService.calculateDeliveryFee(request));
    }
}
