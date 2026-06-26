package at.fhtw.energypctservice.messaging;

import at.fhtw.energycontract.EnergyMessage;
import at.fhtw.energypctservice.service.PercentageEventService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UpdateMessageListener {
    private final PercentageEventService percentageEventService;

    public UpdateMessageListener(PercentageEventService percentageEventService) {
        this.percentageEventService = percentageEventService;
    }

    @RabbitListener(queues = RabbitMqConfig.UPDATE_QUEUE)
    public void receive(EnergyMessage message) {
        percentageEventService.processUpdate(message);
    }
}
