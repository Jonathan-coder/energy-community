package at.fhtw.energyusageservice.messaging;

import at.fhtw.energycontract.EnergyMessage;
import at.fhtw.energyusageservice.service.UsageEventService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EnergyMessageListener {
    private final UsageEventService usageEventService;

    public EnergyMessageListener(UsageEventService usageEventService) {
        this.usageEventService = usageEventService;
    }

    @RabbitListener(queues = RabbitMqConfig.ENERGY_QUEUE)
    public void receive(EnergyMessage message) {
        usageEventService.processMessage(message);
    }
}
