package com.example.fooddelivery.service;

import com.example.fooddelivery.entity.WeatherData;
import com.example.fooddelivery.repository.WeatherDataRepository;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherDataServiceTest {

    @Mock
    private WeatherDataRepository weatherDataRepository;

    private WeatherDataService weatherDataService;

    @BeforeEach
    void setUp() {
        weatherDataService = new WeatherDataService(weatherDataRepository);

        ReflectionTestUtils.setField(weatherDataService, "apiUrl", "https://mocked.api/weather");
        ReflectionTestUtils.setField(weatherDataService, "targetStations", List.of("Tallinn", "Tartu"));
    }

    @Test
    void testFetchAndStoreWeatherData_Success() throws IOException {
        String mockHtml = "<stations>"
                + "<station><name>Tallinn</name><wmocode>12345</wmocode><airtemperature>-2.5</airtemperature>" +
                "<windspeed>3.0</windspeed><phenomenon>snow</phenomenon></station>"
                + "<station><name>Tartu</name><wmocode>67890</wmocode><airtemperature>0.0</airtemperature>" +
                "<windspeed>5.0</windspeed><phenomenon>clear</phenomenon></station>"
                + "</stations>";

        Document mockDocument = Jsoup.parse(mockHtml);

        try (var mockedJsoup = mockStatic(Jsoup.class)) {
            Connection mockConnection = mock(Connection.class);

            when(Jsoup.connect(anyString())).thenReturn(mockConnection);
            when(mockConnection.timeout(anyInt())).thenReturn(mockConnection);
            when(mockConnection.get()).thenReturn(mockDocument);

            when(weatherDataRepository.existsByWmoCodeAndTimestamp(anyInt(), any())).thenReturn(false);

            weatherDataService.fetchAndStoreWeatherData();

            ArgumentCaptor<WeatherData> captor = ArgumentCaptor.forClass(WeatherData.class);
            verify(weatherDataRepository, times(2)).save(captor.capture());

            List<WeatherData> savedWeatherData = captor.getAllValues();

            assertEquals(2, savedWeatherData.size());
            assertEquals("Tallinn", savedWeatherData.get(0).getStationName());
            assertEquals(12345, savedWeatherData.get(0).getWmoCode());
            assertEquals(-2.5, savedWeatherData.get(0).getAirTemperature());
            assertEquals(3.0, savedWeatherData.get(0).getWindSpeed());
            assertEquals("snow", savedWeatherData.get(0).getWeatherPhenomenon());

            assertEquals("Tartu", savedWeatherData.get(1).getStationName());
            assertEquals(67890, savedWeatherData.get(1).getWmoCode());
        }
    }

    @Test
    void testFetchAndStoreWeatherData_NoData() throws IOException {
        String emptyHtml = "<stations></stations>";
        Document mockDocument = Jsoup.parse(emptyHtml);

        try (var mockedJsoup = mockStatic(Jsoup.class)) {
            Connection mockConnection = mock(Connection.class);

            when(Jsoup.connect(anyString())).thenReturn(mockConnection);
            when(mockConnection.timeout(anyInt())).thenReturn(mockConnection);
            when(mockConnection.get()).thenReturn(mockDocument);


            weatherDataService.fetchAndStoreWeatherData();

            verify(weatherDataRepository, never()).save(any());
        }
    }

    @Test
    void testFetchAndStoreWeatherData_DuplicateData() throws IOException {
        String mockHtml = "<stations>"
                + "<station><name>Tallinn</name><wmocode>12345</wmocode><airtemperature>-2.5</airtemperature>" +
                "<windspeed>3.0</windspeed><phenomenon>snow</phenomenon></station>"
                + "</stations>";

        Document mockDocument = Jsoup.parse(mockHtml);

        try (var mockedJsoup = mockStatic(Jsoup.class)) {
            Connection mockConnection = mock(Connection.class);

            when(Jsoup.connect(anyString())).thenReturn(mockConnection);
            when(mockConnection.timeout(anyInt())).thenReturn(mockConnection);
            when(mockConnection.get()).thenReturn(mockDocument);


            when(weatherDataRepository.existsByWmoCodeAndTimestamp(eq(12345), any())).thenReturn(true);

            weatherDataService.fetchAndStoreWeatherData();

            verify(weatherDataRepository, never()).save(any());
        }
    }

    @Test
    void testFetchAndStoreWeatherData_InvalidData() throws IOException {
        String mockHtml = "<stations>"
                + "<station><name>Tallinn</name><wmocode>invalid_code</wmocode>"
                + "<airtemperature>abc</airtemperature><windspeed>xyz</windspeed>"
                + "<phenomenon></phenomenon></station>"
                + "</stations>";

        Document mockDocument = Jsoup.parse(mockHtml);

        try (var mockedJsoup = mockStatic(Jsoup.class)) {
            Connection mockConnection = mock(Connection.class);

            when(Jsoup.connect(anyString())).thenReturn(mockConnection);
            when(mockConnection.timeout(anyInt())).thenReturn(mockConnection);
            when(mockConnection.get()).thenReturn(mockDocument);

            weatherDataService.fetchAndStoreWeatherData();

            verify(weatherDataRepository, never()).save(any());
        }
    }

    @Test
    void testScheduledWeatherDataImport() {
        WeatherDataService spyService = spy(weatherDataService);
        doNothing().when(spyService).fetchAndStoreWeatherData();

        spyService.scheduledWeatherDataImport();

        verify(spyService, times(1)).fetchAndStoreWeatherData();
    }
}
