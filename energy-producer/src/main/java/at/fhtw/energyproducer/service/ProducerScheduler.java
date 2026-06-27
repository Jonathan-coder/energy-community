package at.fhtw.energyproducer.service;

import at.fhtw.energycontract.EnergyAssociation;
import at.fhtw.energycontract.EnergyMessage;
import at.fhtw.energycontract.EnergyMessageType;
import at.fhtw.energycontract.MessagingConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ProducerScheduler {
    private static final String WEATHER_API =
            "https://api.open-meteo.com/v1/forecast?latitude=48.2082&longitude=16.3738&current=weather_code,cloud_cover&timezone=Europe%2FVienna";

    private final RabbitTemplate rabbitTemplate;
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "energy-producer-scheduler");
        return thread;
    });
    private final Random random = new Random();

    public ProducerScheduler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void start() {
        scheduleNextRun();
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
    }

    public void sendProductionMessage() {
        WeatherSnapshot weather = fetchWeather().orElseGet(this::defaultWeatherSnapshot);
        double kwh = calculateProductionKwh(weather);
        rabbitTemplate.convertAndSend(MessagingConstants.ENERGY_QUEUE, new EnergyMessage(
                EnergyMessageType.PRODUCER,
                EnergyAssociation.COMMUNITY,
                kwh,
                LocalDateTime.now().withSecond(0).withNano(0).toString()
        ));
    }

    double calculateProductionKwh(WeatherSnapshot weather) {
        double base = 0.0025 + random.nextDouble() * 0.0045;
        double weatherFactor = 1.0;
        if (weather.weatherCode == 0) {
            weatherFactor += 0.30;
        } else if (weather.weatherCode == 1) {
            weatherFactor += 0.20;
        } else if (weather.weatherCode == 2) {
            weatherFactor += 0.10;
        } else if (weather.weatherCode >= 3 && weather.weatherCode <= 48) {
            weatherFactor -= 0.05;
        } else {
            weatherFactor -= 0.10;
        }

        weatherFactor -= Math.min(0.25, weather.cloudCover / 100.0 * 0.25);
        return round3(Math.max(0.001, base * weatherFactor));
    }

    private Optional<WeatherSnapshot> fetchWeather() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WEATHER_API))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode current = root.path("current");
            if (current.isMissingNode() || current.isNull()) {
                return Optional.empty();
            }

            int weatherCode = current.path("weather_code").asInt(3);
            int cloudCover = current.path("cloud_cover").asInt(50);
            return Optional.of(new WeatherSnapshot(weatherCode, cloudCover));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private WeatherSnapshot defaultWeatherSnapshot() {
        return new WeatherSnapshot(3, 50);
    }

    private void scheduleNextRun() {
        if (scheduler.isShutdown()) {
            return;
        }
        scheduler.schedule(() -> {
            try {
                sendProductionMessage();
            } finally {
                scheduleNextRun();
            }
        }, nextDelayMillis(), TimeUnit.MILLISECONDS);
    }

    private long nextDelayMillis() {
        return 1000L + random.nextInt(4001);
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    record WeatherSnapshot(int weatherCode, int cloudCover) {
    }
}
