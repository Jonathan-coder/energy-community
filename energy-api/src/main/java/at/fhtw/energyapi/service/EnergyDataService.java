package at.fhtw.energyapi.service;

import at.fhtw.energycontract.CurrentPercentage;
import at.fhtw.energycontract.HourlyUsage;
import at.fhtw.energypersistence.model.CurrentPercentageEntity;
import at.fhtw.energypersistence.model.HourlyUsageEntity;
import at.fhtw.energypersistence.repository.CurrentPercentageRepository;
import at.fhtw.energypersistence.repository.HourlyUsageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        return currentPercentageRepository.findTopByOrderByHourDesc()
                .map((CurrentPercentageEntity entity) -> new CurrentPercentage(entity.getHour(), entity.getCommunityDepleted(), entity.getGridPortion()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No percentage data available"));
    }

    public List<HourlyUsage> getHistorical(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start must be before or equal to end");
        }

        String startHour = start.format(FORMATTER);
        String endHour = end.format(FORMATTER);

        return hourlyUsageRepository.findByHourBetweenOrderByHourDesc(startHour, endHour).stream()
                .map(this::toHourlyUsage)
                .toList();
    }

    private HourlyUsage toHourlyUsage(HourlyUsageEntity entity) {
        return new HourlyUsage(entity.getHour(), entity.getCommunityProduced(), entity.getCommunityUsed(), entity.getGridUsed());
    }
}
