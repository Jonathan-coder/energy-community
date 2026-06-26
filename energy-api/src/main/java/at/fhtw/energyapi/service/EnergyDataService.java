package at.fhtw.energyapi.service;

import at.fhtw.energycontract.CurrentPercentage;
import at.fhtw.energycontract.HourlyUsage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

@Service
public class EnergyDataService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final NavigableMap<LocalDateTime, HourlyUsageSnapshot> hourlyData = new TreeMap<>();

    public EnergyDataService() {
        seed();
    }

    public CurrentPercentage getCurrentPercentage() {
        var latestEntry = hourlyData.lastEntry();
        if (latestEntry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No usage data available");
        }
        return toCurrentPercentage(latestEntry.getKey(), latestEntry.getValue());
    }

    public List<HourlyUsage> getHistorical(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start must be before or equal to end");
        }

        var result = new ArrayList<HourlyUsage>();
        for (var entry : hourlyData.subMap(start, true, end, true).descendingMap().entrySet()) {
            result.add(toHourlyUsage(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private void seed() {
        put("2025-01-10T12:00:00", 12.500, 11.800, 0.900);
        put("2025-01-10T13:00:00", 15.015, 14.033, 2.049);
        put("2025-01-10T14:00:00", 18.050, 18.050, 1.076);
    }

    private void put(String hour, double communityProduced, double communityUsed, double gridUsed) {
        hourlyData.put(
                LocalDateTime.parse(hour, FORMATTER),
                new HourlyUsageSnapshot(communityProduced, communityUsed, gridUsed)
        );
    }

    private CurrentPercentage toCurrentPercentage(LocalDateTime hour, HourlyUsageSnapshot snapshot) {
        return new CurrentPercentage(
                hour.format(FORMATTER),
                round2(snapshot.communityProduced == 0.0 ? 0.0 : snapshot.communityUsed / snapshot.communityProduced * 100.0),
                round2(snapshot.communityUsed + snapshot.gridUsed == 0.0
                        ? 0.0
                        : snapshot.gridUsed / (snapshot.communityUsed + snapshot.gridUsed) * 100.0)
        );
    }

    private HourlyUsage toHourlyUsage(LocalDateTime hour, HourlyUsageSnapshot snapshot) {
        return new HourlyUsage(
                hour.format(FORMATTER),
                round3(snapshot.communityProduced),
                round3(snapshot.communityUsed),
                round3(snapshot.gridUsed)
        );
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double round3(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private record HourlyUsageSnapshot(double communityProduced, double communityUsed, double gridUsed) {
    }
}
