package com.example.fooddelivery.service;

import com.example.fooddelivery.entity.WeatherData;
import com.example.fooddelivery.repository.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WeatherDataService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherDataService.class);

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.target.stations}")
    private List<String> targetStations;

    private final WeatherDataRepository weatherDataRepository;

    public WeatherDataService(WeatherDataRepository weatherDataRepository) {
        this.weatherDataRepository = weatherDataRepository;
    }

    public void fetchAndStoreWeatherData() {
        try {
            logger.info("Fetching weather data from {}", apiUrl);
            Document document = Jsoup.connect(apiUrl).get();
            Elements stations = document.select("station");

            List<WeatherData> weatherDataList = stations.stream()
                    .filter(station -> targetStations.contains(getTextOrDefault(station, "name", "Unknown")))
                    .map(this::parseWeatherData)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            for (WeatherData weatherData : weatherDataList) {
                if (weatherDataRepository.countByWmoCodeAndTimestamp(weatherData.getWmoCode(), weatherData.getTimestamp()) == 0) {
                    weatherDataRepository.save(weatherData);
                    logger.info("Saved weather data for {} at {}", weatherData.getStationName(), weatherData.getTimestamp());
                } else {
                    logger.info("Skipping duplicate weather data for {} at {}", weatherData.getStationName(), weatherData.getTimestamp());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to fetch weather data", e);
        }
    }

    @Scheduled(cron = "${weather.cron.expression}")
    public void scheduledWeatherDataImport() {
        logger.info("Running scheduled weather data import...");
        fetchAndStoreWeatherData();
    }

    private Optional<WeatherData> parseWeatherData(Element station) {
        String stationName = getTextOrDefault(station, "name", "Unknown");
        Optional<Integer> wmoCodeOpt = getOptionalInteger(station, "wmocode");
        BigDecimal temperature = getBigDecimalOrDefault(station, "airtemperature");
        BigDecimal windSpeed = getBigDecimalOrDefault(station, "windspeed");
        String weatherPhenomenon = getTextOrDefault(station, "phenomenon", "Unknown");
        LocalDateTime timestamp = LocalDateTime.now();

        if (wmoCodeOpt.isEmpty()) {
            logger.warn("Skipping station {} due to missing WMO code", stationName);
            return Optional.empty();
        }

        WeatherData weatherData = new WeatherData();
        weatherData.setStationName(stationName);
        weatherData.setWmoCode(wmoCodeOpt.get());
        weatherData.setAirTemperature(temperature);
        weatherData.setWindSpeed(windSpeed);
        weatherData.setWeatherPhenomenon(weatherPhenomenon);
        weatherData.setTimestamp(timestamp);

        return Optional.of(weatherData);
    }

    private String getTextOrDefault(Element element, String tag, String defaultValue) {
        Element selected = element.selectFirst(tag);
        return (selected != null) ? selected.text() : defaultValue;
    }

    private BigDecimal getBigDecimalOrDefault(Element element, String tag) {
        try {
            String textValue = getTextOrDefault(element, tag, BigDecimal.ZERO.toString());
            return new BigDecimal(textValue);
        } catch (NumberFormatException e) {
            logger.warn("Invalid number for {}: {}", tag, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private Optional<Integer> getOptionalInteger(Element element, String tag) {
        try {
            return Optional.of(Integer.parseInt(getTextOrDefault(element, tag, "0")));
        } catch (NumberFormatException e) {
            logger.warn("Invalid number for {}: {}", tag, e.getMessage());
            return Optional.empty();
        }
    }
}
