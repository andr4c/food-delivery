package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.DeliveryFeeRequest;
import com.example.fooddelivery.dto.DeliveryFeeResponse;
import com.example.fooddelivery.entity.BaseFee;
import com.example.fooddelivery.entity.ExtraFee;
import com.example.fooddelivery.entity.WeatherData;
import com.example.fooddelivery.exception.InvalidVehicleException;
import com.example.fooddelivery.repository.BaseFeeRepository;
import com.example.fooddelivery.repository.ExtraFeeRepository;
import com.example.fooddelivery.repository.WeatherDataRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class DeliveryFeeService {

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

    public DeliveryFeeResponse calculateDeliveryFee(DeliveryFeeRequest deliveryFeeRequest) {
        String city = deliveryFeeRequest.getCity();
        String vehicleType = deliveryFeeRequest.getVehicleType();

        // Fetch base fee from database
        BaseFee baseFee = baseFeeRepository.findByCityAndVehicleType(city, vehicleType)
                .orElseThrow(() -> new IllegalArgumentException("Invalid city or vehicle type"));

        double totalFee = baseFee.getFee().doubleValue();
        double extraFee = 0.0;

        Optional<WeatherData> latestWeatherData = weatherDataRepository
                .findFirstByStationNameOrderByTimestampDesc(city);

        if (latestWeatherData.isPresent()) {
            WeatherData weatherData = latestWeatherData.get();

            BigDecimal airTemperature = weatherData.getAirTemperature();

            // Extra fee based on air temperature (ATEF) in a specific city is paid in case
            // Vehicle type = Scooter or Bike and:
            // Air temperature is less than -10, then ATEF = 1€
            // Air temperature is between -10 and 0, then ATEF = 0.5€
            List<ExtraFee> airTempFees = extraFeeRepository.findByConditionType("air_temperature");
            for (ExtraFee airTempFee : airTempFees) {

                BigDecimal airTempFeeConditionValue = new BigDecimal(airTempFee.getConditionValue());
                if (airTemperature.compareTo(airTempFeeConditionValue) < 0) {
                    extraFee += airTempFee.getFee().doubleValue();
                }
            }

            BigDecimal windSpeed = weatherData.getWindSpeed();

            // Extra fee based on wind speed (WSEF) in a specific city is paid in case
            // Vehicle type = Bike and:
            // Wind speed is between 10 m/s and 20 m/s, then WSEF = 0.5€
            // If wind speed is greater than 20 m/s -> throw error
            List<ExtraFee> windSpeedFees = extraFeeRepository.findByConditionType("wind_speed");
            for (ExtraFee windSpeedFee : windSpeedFees) {

                BigDecimal windSpeedFeeConditionValue = new BigDecimal(windSpeedFee.getConditionValue());
                if (windSpeed.compareTo(windSpeedFeeConditionValue) > 0) {
                    if (Boolean.TRUE.equals(windSpeedFee.getIsForbidden())) {
                        throw new InvalidVehicleException("Usage of selected vehicle type is forbidden");
                    }
                    extraFee += windSpeedFee.getFee().doubleValue();

                }
            }

            String weatherPhenom = weatherData.getWeatherPhenomenon();

            // Extra fee based on weather phenomenon (WPEF) in a specific city is paid in case
            // Vehicle type = Scooter or Bike and:
            // Weather phenomenon is related to snow or sleet, then WPEF = 1€
            // In case of glaze, hail, or thunder -> throw error
            List<ExtraFee> weatherPhenomenonFees = extraFeeRepository.findByConditionType("weather_phenomenon");
            for (ExtraFee weatherPhenomFee : weatherPhenomenonFees) {

                String weatherPhenomFeeConditionValue = weatherData.getWeatherPhenomenon().toLowerCase();
                if (weatherPhenom.contains(weatherPhenomFeeConditionValue)) {
                    if (Boolean.TRUE.equals(weatherPhenomFee.getIsForbidden())) {
                        throw new InvalidVehicleException("Usage of selected vehicle type is forbidden");
                    }

                    extraFee += weatherPhenomFee.getFee().doubleValue();
                }
            }
        }

        return new DeliveryFeeResponse("Total delivery fee: ", totalFee + extraFee);
    }

}
