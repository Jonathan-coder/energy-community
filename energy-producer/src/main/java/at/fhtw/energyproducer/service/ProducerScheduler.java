package at.fhtw.energyproducer.service;

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
public class ProducerScheduler {
    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public ProducerScheduler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${producer.interval:3000}")
    public void sendProductionMessage() {
        double base = 10 + random.nextDouble() * 6;
        double weatherFactor = isSunny() ? 1.15 : 0.85;
        double kwh = Math.round((base * weatherFactor) * 100.0) / 100.0;
        rabbitTemplate.convertAndSend(MessagingConstants.ENERGY_QUEUE, new EnergyMessage(
                EnergyMessageType.PRODUCER,
                EnergyAssociation.COMMUNITY,
                kwh,
                LocalDateTime.now().withSecond(0).withNano(0).toString()
        ));
    }

    private boolean isSunny() {
        return random.nextBoolean();
    }
}
