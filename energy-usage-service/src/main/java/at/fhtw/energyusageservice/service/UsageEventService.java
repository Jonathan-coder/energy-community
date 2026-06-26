package at.fhtw.energyusageservice.service;

import at.fhtw.energycontract.EnergyMessage;
import at.fhtw.energycontract.EnergyMessageType;
import at.fhtw.energycommon.EnergyCalculationService;
import at.fhtw.energypersistence.model.HourlyUsageEntity;
import at.fhtw.energypersistence.repository.HourlyUsageRepository;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsageEventService {
    private final HourlyUsageRepository repository;
    private final EnergyCalculationService calculationService;
    private final RabbitTemplate rabbitTemplate;
    private final Queue updateQueue;

    public UsageEventService(HourlyUsageRepository repository,
                             EnergyCalculationService calculationService,
                             RabbitTemplate rabbitTemplate,
                             Queue updateQueue) {
        this.repository = repository;
        this.calculationService = calculationService;
        this.rabbitTemplate = rabbitTemplate;
        this.updateQueue = updateQueue;
    }

    public void processMessage(EnergyMessage message) {
        String hour = normalizeHour(message.datetime());
        Optional<HourlyUsageEntity> existing = repository.findByHour(hour);

        double communityProduced = 0.0;
        double communityUsed = 0.0;
        double gridUsed = 0.0;

        if (existing.isPresent()) {
            var entity = existing.get();
            communityProduced = entity.getCommunityProduced();
            communityUsed = entity.getCommunityUsed();
            gridUsed = entity.getGridUsed();
        }

        if (message.type() == EnergyMessageType.PRODUCER) {
            communityProduced += message.kwh();
        } else if (message.type() == EnergyMessageType.USER) {
            communityUsed += message.kwh();
        }

        double effectiveCommunityUsed = Math.min(communityUsed, communityProduced);
        double effectiveGridUsed = Math.max(0.0, communityUsed - effectiveCommunityUsed) + gridUsed;
        var usage = calculationService.calculateUsage(hour, communityProduced, communityUsed, effectiveGridUsed);

        if (existing.isPresent()) {
            var entity = existing.get();
            entity.setCommunityProduced(usage.communityProduced());
            entity.setCommunityUsed(usage.communityUsed());
            entity.setGridUsed(usage.gridUsed());
            repository.save(entity);
        } else {
            repository.save(new HourlyUsageEntity(hour, usage.communityProduced(), usage.communityUsed(), usage.gridUsed()));
        }

        rabbitTemplate.convertAndSend(updateQueue.getName(), new EnergyMessage(EnergyMessageType.UPDATE, null, usage.communityProduced(), hour));
    }

    private String normalizeHour(String datetime) {
        return datetime.length() > 13 ? datetime.substring(0, 13) : datetime;
    }
}
