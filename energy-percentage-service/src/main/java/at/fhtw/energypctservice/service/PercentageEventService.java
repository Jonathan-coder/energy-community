package at.fhtw.energypctservice.service;

import at.fhtw.energycommon.EnergyCalculationService;
import at.fhtw.energycontract.EnergyMessage;
import at.fhtw.energycontract.EnergyMessageType;
import at.fhtw.energypersistence.model.CurrentPercentageEntity;
import at.fhtw.energypersistence.repository.CurrentPercentageRepository;
import at.fhtw.energypersistence.repository.HourlyUsageRepository;
import org.springframework.stereotype.Service;

@Service
public class PercentageEventService {
    private final CurrentPercentageRepository currentPercentageRepository;
    private final HourlyUsageRepository hourlyUsageRepository;
    private final EnergyCalculationService calculationService;

    public PercentageEventService(CurrentPercentageRepository currentPercentageRepository,
                                  HourlyUsageRepository hourlyUsageRepository,
                                  EnergyCalculationService calculationService) {
        this.currentPercentageRepository = currentPercentageRepository;
        this.hourlyUsageRepository = hourlyUsageRepository;
        this.calculationService = calculationService;
    }

    public void processUpdate(EnergyMessage message) {
        if (message.type() != EnergyMessageType.UPDATE) {
            return;
        }

        String hour = normalizeHour(message.datetime());
        var hourlyUsage = hourlyUsageRepository.findByHour(hour);
        if (hourlyUsage.isEmpty()) {
            return;
        }

        var percentage = calculationService.calculatePercentage(
                hour,
                hourlyUsage.get().getCommunityProduced(),
                hourlyUsage.get().getCommunityUsed(),
                hourlyUsage.get().getGridUsed()
        );

        currentPercentageRepository.deleteAll();
        currentPercentageRepository.save(new CurrentPercentageEntity(
                percentage.hour(),
                percentage.communityDepleted(),
                percentage.gridPortion()
        ));
    }

    private String normalizeHour(String datetime) {
        return datetime.length() > 13 ? datetime.substring(0, 13) : datetime;
    }
}
