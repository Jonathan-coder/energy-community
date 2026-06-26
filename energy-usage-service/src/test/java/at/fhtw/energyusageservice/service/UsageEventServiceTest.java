package at.fhtw.energyusageservice.service;

import at.fhtw.energycommon.EnergyCalculationService;
import at.fhtw.energycontract.EnergyAssociation;
import at.fhtw.energycontract.EnergyMessage;
import at.fhtw.energycontract.EnergyMessageType;
import at.fhtw.energypersistence.model.HourlyUsageEntity;
import at.fhtw.energypersistence.repository.HourlyUsageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsageEventServiceTest {

    @Test
    void accumulatesProducerAndUserMessagesIntoCommunityAndGridUsage() {
        HourlyUsageRepository repository = mock(HourlyUsageRepository.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        Queue updateQueue = mock(Queue.class);
        when(updateQueue.getName()).thenReturn("energy.update.queue");
        when(repository.findByHour("2026-06-26T10")).thenReturn(Optional.of(new HourlyUsageEntity("2026-06-26T10", 3.0, 1.0, 0.0)));

        UsageEventService service = new UsageEventService(repository, new EnergyCalculationService(), rabbitTemplate, updateQueue);

        service.processMessage(new EnergyMessage(EnergyMessageType.PRODUCER, EnergyAssociation.COMMUNITY, 4.0, "2026-06-26T10:30:00"));

        verify(repository).save(any(HourlyUsageEntity.class));
    }

    @Test
    void sendsUpdateEventAfterProcessing() {
        HourlyUsageRepository repository = mock(HourlyUsageRepository.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        Queue updateQueue = mock(Queue.class);
        when(updateQueue.getName()).thenReturn("energy.update.queue");
        when(repository.findByHour("2026-06-26T10")).thenReturn(Optional.empty());

        UsageEventService service = new UsageEventService(repository, new EnergyCalculationService(), rabbitTemplate, updateQueue);

        service.processMessage(new EnergyMessage(EnergyMessageType.USER, EnergyAssociation.COMMUNITY, 2.5, "2026-06-26T10:30:00"));

        verify(rabbitTemplate).convertAndSend(any(), any(EnergyMessage.class));
    }
}
