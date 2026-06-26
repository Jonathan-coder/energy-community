package at.fhtw.energyapi.service;

import at.fhtw.energycontract.CurrentPercentage;
import at.fhtw.energycontract.HourlyUsage;
import at.fhtw.energypersistence.model.HourlyUsageEntity;
import at.fhtw.energypersistence.repository.CurrentPercentageRepository;
import at.fhtw.energypersistence.repository.HourlyUsageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnergyDataService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final HourlyUsageRepository hourlyUsageRepository;
    private final CurrentPercentageRepository currentPercentageRepository;

    public EnergyDataService(HourlyUsageRepository hourlyUsageRepository,
                             CurrentPercentageRepository currentPercentageRepository) {
        this.hourlyUsageRepository = hourlyUsageRepository;
        this.currentPercentageRepository = currentPercentageRepository;
    }

    public CurrentPercentage getCurrentPercentage() {
        var latestEntry = currentPercentageRepository.findAll().stream()
                .reduce((first, second) -> second)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No percentage data available"));
        return new CurrentPercentage(latestEntry.getHour(), latestEntry.getCommunityDepleted(), latestEntry.getGridPortion());
    }

    public List<HourlyUsage> getHistorical(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start must be before or equal to end");
        }

        return hourlyUsageRepository.findAll().stream()
                .filter(entity -> {
                    var value = LocalDateTime.parse(entity.getHour(), FORMATTER);
                    return !value.isBefore(start) && !value.isAfter(end);
                })
                .sorted((left, right) -> right.getHour().compareTo(left.getHour()))
                .map(this::toHourlyUsage)
                .collect(Collectors.toList());
    }

    private HourlyUsage toHourlyUsage(HourlyUsageEntity entity) {
        return new HourlyUsage(entity.getHour(), entity.getCommunityProduced(), entity.getCommunityUsed(), entity.getGridUsed());
    }
}
