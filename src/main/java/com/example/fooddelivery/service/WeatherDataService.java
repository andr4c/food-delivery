package com.example.fooddelivery.service;

import com.example.fooddelivery.entity.WeatherData;
import com.example.fooddelivery.repository.WeatherDataRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    /**
     * Fetches weather data from the external API, parses it, and stores new records in the database.
     */
    public void fetchAndStoreWeatherData() {
        try {
            logger.info("Fetching weather data from {}", apiUrl);
            Document document = Jsoup.connect(apiUrl).timeout(10_000).get();
            Elements stations = document.select("station");

            List<WeatherData> weatherDataList = stations.stream()
                    .filter(station -> targetStations.contains(getTextOrNull(station, "name")))
                    .map(this::parseWeatherData)
                    .filter(Objects::nonNull)
                    .toList();

            if (weatherDataList.isEmpty()) {
                logger.warn("No weather data found from API. Possible issue with {}", apiUrl);
            } else {
                logger.info("Parsed {} weather records", weatherDataList.size());
            }

            for (WeatherData weatherData : weatherDataList) {
                if (Boolean.FALSE.equals(weatherDataRepository.existsByWmoCodeAndTimestamp(
                        weatherData.getWmoCode(), weatherData.getTimestamp()))) {
                    weatherDataRepository.save(weatherData);
                    logger.info("Saved weather data for {} at {}",
                            weatherData.getStationName(), weatherData.getTimestamp());
                } else {
                    logger.debug("Skipping duplicate weather data for {} at {}",
                            weatherData.getStationName(), weatherData.getTimestamp());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to fetch weather data from {}: {}", apiUrl, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during weather data fetching", e);
        }
    }

    /**
     * Scheduled method to periodically import weather data based on a cron expression.
     */
    @Scheduled(cron = "${weather.cron.expression}")
    public void scheduledWeatherDataImport() {
        logger.info("Running scheduled weather data import...");
        fetchAndStoreWeatherData();
    }

    /**
     * Parses weather data from an HTML element.
     *
     * @param station The HTML element containing weather data.
     * @return A WeatherData object, or null if parsing fails.
     */
    private WeatherData parseWeatherData(Element station) {
        String stationName = getTextOrNull(station, "name");
        Integer wmoCode = getOptionalInteger(station);
        Double temperature = getDoubleOrNull(station, "airtemperature");
        Double windSpeed = getDoubleOrNull(station, "windspeed");
        String weatherPhenomenon = getTextOrNull(station, "phenomenon");

        if (wmoCode == null) {
            logger.warn("Skipping station {} due to missing WMO code", stationName);
            return null;
        }

        WeatherData weatherData = new WeatherData();
        weatherData.setStationName(stationName);
        weatherData.setWmoCode(wmoCode);
        weatherData.setAirTemperature(temperature);
        weatherData.setWindSpeed(windSpeed);
        weatherData.setWeatherPhenomenon(weatherPhenomenon);
        weatherData.setTimestamp(LocalDateTime.now());

        return weatherData;
    }

    /**
     * Retrieves the text content of a given tag or returns null if absent or blank.
     *
     * @param element The parent HTML element.
     * @param tag     The child tag to extract text from.
     * @return The trimmed text content, or null if not found or blank.
     */
    private String getTextOrNull(Element element, String tag) {
        Element selected = element.selectFirst(tag);
        return (selected != null && !selected.text().isBlank()) ? selected.text().trim() : null;
    }

    /**
     * Parses a double value from a given tag or returns null if not present or invalid.
     *
     * @param element The parent HTML element.
     * @param tag     The child tag containing the double value.
     * @return The parsed double value, or null if invalid.
     */
    private Double getDoubleOrNull(Element element, String tag) {
        String textValue = getTextOrNull(element, tag);
        if (textValue == null) {
            return null;
        }
        try {
            return Double.parseDouble(textValue);
        } catch (NumberFormatException e) {
            logger.warn("Invalid number format for {}: {}", tag, textValue);
            return null;
        }
    }

    /**
     * Parses an integer value from the "wmocode" tag or returns null if not present or invalid.
     *
     * @param element The parent HTML element.
     * @return The parsed integer value, or null if invalid.
     */
    private Integer getOptionalInteger(Element element) {
        String textValue = getTextOrNull(element, "wmocode");
        if (textValue == null) {
            return null;
        }
        try {
            return Integer.parseInt(textValue);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer format for {}: {}", "wmocode", textValue);
            return null;
        }
    }
}
