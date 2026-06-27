package at.fhtw.energyuser.service;

import at.fhtw.energycontract.EnergyAssociation;
import at.fhtw.energycontract.EnergyMessage;
import at.fhtw.energycontract.EnergyMessageType;
import at.fhtw.energycontract.MessagingConstants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class UserScheduler {
    private final RabbitTemplate rabbitTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "energy-user-scheduler");
        return thread;
    });
    private final Random random = new Random();

    public UserScheduler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void start() {
        scheduleNextRun();
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
    }

    public void sendUsageMessage() {
        int hour = LocalDateTime.now().getHour();
        double factor = hour < 6 ? 0.70 : hour < 10 ? 1.10 : hour < 16 ? 0.85 : hour < 22 ? 1.35 : 0.75;
        double kwh = calculateUsageKwh(factor);
        rabbitTemplate.convertAndSend(MessagingConstants.ENERGY_QUEUE, new EnergyMessage(
                EnergyMessageType.USER,
                EnergyAssociation.COMMUNITY,
                kwh,
                LocalDateTime.now().withSecond(0).withNano(0).toString()
        ));
    }

    double calculateUsageKwh(double factor) {
        double base = 0.0015 + random.nextDouble() * 0.0040;
        return round3(Math.max(0.001, base * factor));
    }

    private void scheduleNextRun() {
        if (scheduler.isShutdown()) {
            return;
        }
        scheduler.schedule(() -> {
            try {
                sendUsageMessage();
            } finally {
                scheduleNextRun();
            }
        }, nextDelayMillis(), TimeUnit.MILLISECONDS);
    }

    private long nextDelayMillis() {
        return 1000L + random.nextInt(4001);
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
