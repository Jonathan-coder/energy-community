package at.fhtw.energyuser.service;

import at.fhtw.energycontract.EnergyAssociation;
import at.fhtw.energycontract.EnergyMessage;
import at.fhtw.energycontract.EnergyMessageType;
import at.fhtw.energycontract.MessagingConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserScheduler {
    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public UserScheduler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${user.interval:4000}")
    public void sendUsageMessage() {
        int hour = LocalDateTime.now().getHour();
        double factor = hour < 6 ? 0.6 : hour < 12 ? 0.9 : hour < 18 ? 1.2 : 0.8;
        double kwh = Math.round((2.5 + random.nextDouble() * 1.2) * factor * 100.0) / 100.0;
        rabbitTemplate.convertAndSend(MessagingConstants.ENERGY_QUEUE, new EnergyMessage(
                EnergyMessageType.USER,
                EnergyAssociation.COMMUNITY,
                kwh,
                LocalDateTime.now().withSecond(0).withNano(0).toString()
        ));
    }
}
