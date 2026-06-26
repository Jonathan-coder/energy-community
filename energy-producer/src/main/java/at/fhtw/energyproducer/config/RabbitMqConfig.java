package at.fhtw.energyproducer.config;

import at.fhtw.energycontract.MessagingConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String ENERGY_QUEUE = MessagingConstants.ENERGY_QUEUE;

    @Bean
    public Queue energyQueue() {
        return new Queue(ENERGY_QUEUE, true, false, false);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
