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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryFeeService {

    private static final String USAGE_FORBIDDEN_MESSAGE =
            "Usage of selected vehicle type is forbidden due to weather conditions";

    private static final Logger logger = LoggerFactory.getLogger(DeliveryFeeService.class);

    private final BaseFeeRepository baseFeeRepository;
    private final ExtraFeeRepository extraFeeRepository;
    private final WeatherDataRepository weatherDataRepository;

    public DeliveryFeeService(BaseFeeRepository baseFeeRepository,
                              ExtraFeeRepository extraFeeRepository,
                              WeatherDataRepository weatherDataRepository) {
        this.baseFeeRepository = baseFeeRepository;
        this.extraFeeRepository = extraFeeRepository;
        this.weatherDataRepository = weatherDataRepository;
    }

    /**
     * Calculates the total delivery fee based on base fees and applicable extra fees.
     *
     * @param deliveryFeeRequest The request containing city and vehicle type.
     * @return DeliveryFeeResponse containing the total fee.
     */
    public DeliveryFeeResponse calculateDeliveryFee(DeliveryFeeRequest deliveryFeeRequest) {
        String city = deliveryFeeRequest.getCity();
        String vehicleType = deliveryFeeRequest.getVehicleType();

        Double baseFee = getBaseFee(city, vehicleType);
        Double extraFee = getExtraFees(city, vehicleType);

        Double totalFee = baseFee + extraFee;

        return new DeliveryFeeResponse(String.format("Total delivery fee: %.2f", totalFee), totalFee);
    }

    /**
     * Retrieves the base fee for a given city and vehicle type from the database.
     *
     * @param city        The city for delivery.
     * @param vehicleType The type of vehicle.
     * @return The base fee amount.
     */
    public Double getBaseFee(String city, String vehicleType) {
        return baseFeeRepository.findByCityAndVehicleType(city, vehicleType)
                .map(BaseFee::getFee)
                .orElseThrow(() -> new BaseFeeNotFoundException(
                        "No base fee found for city: " + city + " and vehicle type: " + vehicleType)
                );
    }

    /**
     * Calculates the extra fees based on weather conditions and stored fee rules.
     *
     * @param city        The city for delivery.
     * @param vehicleType The type of vehicle.
     * @return The extra fee amount.
     */
    private Double getExtraFees(String city, String vehicleType) {
        Optional<WeatherData> latestWeatherData = weatherDataRepository.findByStationNameContaining(city);

        if (latestWeatherData.isEmpty()) {
            logger.warn("No weather data found for city: {}", city);
            return 0.0;
        }

        WeatherData weatherData = latestWeatherData.get();

        if (isForbidden(vehicleType, weatherData)) {
            throw new InvalidVehicleException(USAGE_FORBIDDEN_MESSAGE);
        }

        Double airTemperatureFee = getAirTemperatureFee(vehicleType, weatherData.getAirTemperature());
        Double windSpeedFee = getWindSpeedFee(vehicleType, weatherData.getWindSpeed());
        Double weatherPhenomenonFee = getWeatherPhenomenonFee(vehicleType, weatherData.getWeatherPhenomenon());

        return airTemperatureFee + windSpeedFee + weatherPhenomenonFee;
    }

    /**
     * Calculates the extra fee based on air temperature.
     *
     * @param vehicleType    The type of vehicle used for delivery.
     * @param airTemperature The recorded air temperature.
     * @return Extra fee based on air temperature conditions.
     */
    Double getAirTemperatureFee(String vehicleType, Double airTemperature) {
        if (airTemperature == null || vehicleType == null) {
            return 0.0;
        }

        List<ExtraFee> airTempFees = extraFeeRepository.findByConditionTypeAndVehicleType(
                "air_temperature", vehicleType
        );

        for (ExtraFee fee : airTempFees) {
            if (airTemperature > fee.getMinValue() && airTemperature < fee.getMaxValue()) {
                return fee.getFee();
            }
        }

        return 0.0;
    }

    /**
     * Calculates extra fee based on wind speed.
     *
     * @param vehicleType The type of vehicle.
     * @param windSpeed   The recorded wind speed.
     * @return The applicable extra fee.
     */
    Double getWindSpeedFee(String vehicleType, Double windSpeed) {
        if (windSpeed == null || vehicleType == null) {
            return 0.0;
        }

        List<ExtraFee> windSpeedFees = extraFeeRepository.findByConditionTypeAndVehicleType(
                "wind_speed", vehicleType
        );

        for (ExtraFee fee : windSpeedFees) {
            if (windSpeed > fee.getMaxValue()) {  // Forbidden case
                logger.error("Vehicle type {} is forbidden due to wind speed {} m/s", vehicleType, windSpeed);

                throw new InvalidVehicleException(USAGE_FORBIDDEN_MESSAGE);
            }

            if (windSpeed > fee.getMinValue() && windSpeed < fee.getMaxValue()) {
                return fee.getFee();
            }
        }

        return 0.0;
    }

    /**
     * Calculates extra fee based on weather phenomenon conditions.
     *
     * @param vehicleType       The type of vehicle.
     * @param weatherPhenomenon The weather phenomenon.
     * @return The applicable extra fee.
     */
    Double getWeatherPhenomenonFee(String vehicleType, String weatherPhenomenon) {
        if (weatherPhenomenon == null || vehicleType == null) {
            return 0.0;
        }

        List<ExtraFee> weatherPhenomenonFees = extraFeeRepository.findByConditionTypeAndVehicleType(
                "weather_phenomenon", vehicleType
        );

        for (ExtraFee fee : weatherPhenomenonFees) {
            if (fee.getWeatherPhenomenon().trim().equalsIgnoreCase(weatherPhenomenon.trim())) {
                if (Boolean.TRUE.equals(fee.getIsForbidden())) {
                    logger.error("Vehicle type {} is forbidden due to weather phenomenon '{}'",
                            vehicleType, weatherPhenomenon);

                    throw new InvalidVehicleException(USAGE_FORBIDDEN_MESSAGE);
                }

                return fee.getFee();
            }
        }

        return 0.0;
    }

    /**
     * Checks if a given vehicle type is forbidden based on weather conditions.
     *
     * @param vehicleType The type of vehicle.
     * @param weatherData The current weather data.
     * @return true if the vehicle type is forbidden under the given weather conditions, false otherwise.
     */
    private boolean isForbidden(String vehicleType, WeatherData weatherData) {
        return extraFeeRepository.findByConditionTypeAndVehicleType("weather_phenomenon", vehicleType)
                .stream()
                .anyMatch(fee -> fee.getIsForbidden()
                        && fee.getWeatherPhenomenon().equalsIgnoreCase(weatherData.getWeatherPhenomenon()));
    }
}
